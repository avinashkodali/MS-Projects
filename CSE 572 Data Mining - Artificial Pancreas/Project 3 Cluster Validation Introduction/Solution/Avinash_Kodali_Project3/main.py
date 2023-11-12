import pandas as pd
import numpy as np
from datetime import timedelta
from scipy.stats import entropy,iqr
from scipy.fftpack import rfft
from scipy.signal import periodogram
import math
from sklearn.metrics import confusion_matrix,mean_squared_error
from sklearn import metrics
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans,DBSCAN

import warnings
warnings.filterwarnings("ignore")

def create_meal_data(insulin_df,cgm_df):
    '''
    Function is used to create a meal dataframe from insulin and cgm dataframe
    '''
    insulin_df=insulin_df.replace(0.0,np.nan).dropna().reset_index(drop=True)
    
    valid_meal_datetimestamps=[]

    for idx,datetimestamp in enumerate(insulin_df['date_time_stamp']):
        try:
            hours=(insulin_df['date_time_stamp'][idx+1]-datetimestamp).seconds / 60.0
            if hours >= 120:
                valid_meal_datetimestamps.append(datetimestamp)
        except KeyError:
            break
    meal_list=[]

    for idx,datetimestamp in enumerate(valid_meal_datetimestamps):
        start_time=pd.to_datetime(datetimestamp - timedelta(minutes=30))
        end_time=pd.to_datetime(datetimestamp + timedelta(minutes=120))
        date=datetimestamp.date().strftime('%#m/%#d/%Y')
        meal_list.append(cgm_df.loc[cgm_df['Date']==date].set_index('date_time_stamp').between_time(start_time=start_time.strftime('%#H:%#M:%#S'),end_time=end_time.strftime('%#H:%#M:%#S'))['Sensor Glucose (mg/dL)'].values.tolist())
    meal_df=pd.DataFrame(meal_list)
    meal_df["date_time_stamp"]=valid_meal_datetimestamps
    return meal_df

    
def create_meal_features(meal_data):
    '''
    Function is used to create a meal feature dataframe
    '''
    idx=meal_data.isna().sum(axis=1).replace(0,np.nan).dropna().where(lambda x:x>6).dropna().index 
    meal_cleaned_df=meal_data.drop(meal_data.index[idx]).reset_index().drop(columns='index')

    datetimestamp=meal_cleaned_df["date_time_stamp"]

    meal_cleaned_df=meal_cleaned_df.iloc[:,0:31].interpolate(method='linear',axis=1)
    meal_cleaned_df["date_time_stamp"]=datetimestamp
    


    idx_to_drop=meal_cleaned_df.isna().sum(axis=1).replace(0,np.nan).dropna().index
    meal_cleaned_df=meal_cleaned_df.drop(meal_data.index[idx_to_drop]).reset_index().drop(columns='index')
    meal_cleaned_df=meal_cleaned_df.dropna().reset_index().drop(columns='index')
    meal_feature_df=pd.DataFrame()

    min_velocity=[]
    max_velocity=[]
    mean_velocity=[]

    min_acceleration=[]
    max_acceleration=[]
    mean_acceleration=[]

    minimum_idx = 6
    maximum_idx=meal_cleaned_df.iloc[:,7:31].idxmax(axis=1)

    for i in range(len(meal_cleaned_df)):
        first_differential_data=np.diff(meal_cleaned_df.iloc[i,minimum_idx:(maximum_idx[i]+1)].tolist())
        min_velocity.append(first_differential_data.min())
        max_velocity.append(first_differential_data.max())
        mean_velocity.append(first_differential_data.mean())

        if(len(meal_cleaned_df.iloc[i,minimum_idx:(maximum_idx[i]+1)])>2):
            second_differential_data=np.diff(np.diff(meal_cleaned_df.iloc[i,minimum_idx:maximum_idx[i]+1].tolist()))
            min_acceleration.append(second_differential_data.min())
            max_acceleration.append(second_differential_data.max())
            mean_acceleration.append(second_differential_data.mean())
        else:
            min_acceleration.append(0)
            max_acceleration.append(0)
            mean_acceleration.append(0) 

    meal_feature_df['min_velocity']=min_velocity
    meal_feature_df['max_velocity']=max_velocity
    meal_feature_df['mean_velocity']=mean_velocity

    meal_feature_df['min_acceleration']=min_acceleration
    meal_feature_df['max_acceleration']=max_acceleration
    meal_feature_df['mean_acceleration']=mean_acceleration
 
    entropy_list=[]
    for i in range(len(meal_cleaned_df)):
        entropy_list.append(entropy(np.array(meal_cleaned_df.iloc[i,:31]).astype(float)))
    meal_feature_df['entropy']=entropy_list

    iqr_list=[]
    for i in range(len(meal_cleaned_df)):
        iqr_list.append(iqr(meal_cleaned_df.iloc[i,:31]))
    meal_feature_df['iqr']=iqr_list

    first_power_max=[]
    second_power_max=[]
    third_power_max=[]
    fourth_power_max=[]
    fifth_power_max=[]
    sixth_power_max=[]
    for i in range(len(meal_cleaned_df)):
        rfft_list_sorted=abs(rfft(meal_cleaned_df.iloc[:,0:31].iloc[i].values.tolist())).tolist()
        rfft_list_sorted.sort()
        first_power_max.append(rfft_list_sorted[-2])
        second_power_max.append(rfft_list_sorted[-3])
        third_power_max.append(rfft_list_sorted[-4])
        fourth_power_max.append(rfft_list_sorted[-5])
        fifth_power_max.append(rfft_list_sorted[-6])
        sixth_power_max.append(rfft_list_sorted[-7])
    
    meal_feature_df['first_power_max']=first_power_max
    meal_feature_df['second_power_max']=second_power_max
    meal_feature_df['third_power_max']=third_power_max
    meal_feature_df['fourth_power_max']=fourth_power_max
    meal_feature_df['fifth_power_max']=fifth_power_max
    meal_feature_df['sixth_power_max']=sixth_power_max

    psd1_list=[]
    psd2_list=[]
    psd3_list=[]

    for i in range(len(meal_cleaned_df)):
        psd=periodogram(meal_cleaned_df.iloc[:,0:31].iloc[i])
        psd1_list.append(psd[1][0:5].mean())
        psd2_list.append(psd[1][5:10].mean())
        psd3_list.append(psd[1][10:16].mean())

    meal_feature_df['psd1']=psd1_list
    meal_feature_df['psd2']=psd2_list
    meal_feature_df['psd3']=psd3_list

    meal_feature_df["date_time_stamp"]=meal_cleaned_df["date_time_stamp"]
    return meal_feature_df


def ground_truth_extraction(meal_feature_df,insulin_data_df):
    '''
    Function is used to extract the ground truth clusters and bins
    '''
    insulin_carbs_df=insulin_data_df.loc[insulin_data_df["date_time_stamp"].isin(meal_feature_df["date_time_stamp"])]["BWZ Carb Input (grams)"].dropna()
    minValue = insulin_carbs_df.min()
    maxValue = insulin_carbs_df.max()
    number_of_bins = math.ceil((maxValue-minValue)/20)
    bins_list=[] 
    clusters_list=[]
    for idx in range(number_of_bins):
        if idx==0:
            bins_list=[minValue-1]
        else:
            bins_list.append(bins_list[idx-1]+20)
        clusters_list.append(idx)
    bins_list.append(maxValue)
    ground_truth_df=pd.cut(x = insulin_carbs_df,bins = bins_list, labels = clusters_list).reset_index(drop=True)
    return meal_feature_df.iloc[:,:-1],ground_truth_df

def k_means(X, number_of_clusters):
    '''
    Function for kmeans clustering algorithm
    '''
    cluster = KMeans(n_clusters = number_of_clusters)
    cluster.fit(X)
    predicted_clusters = cluster.predict(X)
    return predicted_clusters

def DBSCAN_clustering(matrix, eps, min_samples):
    '''
    Function for DBSCAN clustering
    '''
    scaler = StandardScaler()
    X = scaler.fit_transform(matrix)
    cluster = DBSCAN(eps=eps, min_samples = min_samples)
    cluster.fit(X)
    predicted_clusters=cluster.labels_
    return predicted_clusters

def calculate_SSE(meal_feature_noramlized_df,y_pred):
    '''
    Function is used to calculate the SSE value
    '''
    sse_value=0
    for i in np.unique(y_pred):
        idx=np.where(y_pred==i)[0].tolist()
        x=meal_feature_noramlized_df[idx,:]
        sse_value+=((x - x.mean()) ** 2).sum(axis=1).sum()
    return sse_value

def calculate_entropy(confusion_matrix):
    '''
    Function is used to calculate the entropy value
    '''
    x = []
    entropy_list = []
    for i in range(len(confusion_matrix)):
        x.insert(i,sum(confusion_matrix[i]))
        entropy_list.insert(i,0)
        y = []
        for j in range(len(confusion_matrix[i])):
            if x[i]!=0:         
                value = confusion_matrix[i][j]/x[i]
                if value!=0:
                    y.insert(j,value)
        entropy_list[i]=entropy(y,base=2)
    total = sum(x)
    entropy_value=0
    for i in range(len(entropy_list)):
        entropy_value = ((x[i]/total)*entropy_list[i])+entropy_value
    return entropy_value

def calculate_purity(confusion_matrix):
    '''
    This function is used to calculate the purity
    '''
    purity = np.amax(confusion_matrix,axis=1).sum()/confusion_matrix.sum()
    return purity




if __name__=="__main__":

    print('Executing cluster validation ...')

    # Reading Insulin Data
    print('     Reading data from Insulin.csv')
    insulin_df=pd.read_csv('InsulinData.csv',low_memory=False,usecols=['Date','Time','BWZ Carb Input (grams)'])
    insulin_df['date_time_stamp']=pd.to_datetime(insulin_df['Date'] + ' ' + insulin_df['Time'])
    insulin_df=insulin_df.sort_values(by='date_time_stamp',ascending=True).reset_index(drop=True)

    #Reading CGM data
    print('     Reading data from CGMData.csv')
    cgm_df=pd.read_csv('CGMData.csv',low_memory=False,usecols=['Date','Time','Sensor Glucose (mg/dL)'])
    cgm_df['date_time_stamp']=pd.to_datetime(cgm_df['Date'] + ' ' + cgm_df['Time'])
    cgm_df=cgm_df.sort_values(by='date_time_stamp',ascending=True).reset_index(drop=True)

    #Creating Meal Data
    print('     Creation of Meal data')
    meal_df=create_meal_data(insulin_df,cgm_df)#.iloc[:,0:30]

    #Create meal feature matrix
    print('     Creating meal feature matrix')
    meal_feature_df=create_meal_features(meal_df)

    #Extracting ground truth
    print('     Extracting the ground truth clusters and bins')
    meal_feature_df,ground_truth_df=ground_truth_extraction(meal_feature_df,insulin_df)

    clusters = ground_truth_df.nunique()
    
    ground_truth_df=ground_truth_df.to_numpy()
    if clusters <= 1:
        clusters=1

    scaler = StandardScaler()
    meal_feature_normalized_df = scaler.fit_transform(meal_feature_df)

    # kmeans clustering   
    print("     K-means clustering")
    kmeans_pred_clusters = k_means(meal_feature_normalized_df,clusters)
    kmeans_confustion_matrix = confusion_matrix(ground_truth_df, kmeans_pred_clusters)
    print("         Bin and Cluster Matrix for kmeans: ")
    print(kmeans_confustion_matrix)

    print("         Caluculating K-means SSE")
    kmeans_SSE = calculate_SSE(meal_feature_normalized_df,kmeans_pred_clusters)
   
    print("         Caluculating K-means entropy")
    kmeans_entropy = calculate_entropy(kmeans_confustion_matrix)

    print("         Caluculating K-means purity")
    kmeans_purity = calculate_purity(kmeans_confustion_matrix)

    # DBSCAN clustering
    print("     DBSCAN clustering")
    DBSCAN_pred_clusters = DBSCAN_clustering(meal_feature_normalized_df,0.85,3)
    DBSCAN_confustion_matrix = confusion_matrix(ground_truth_df,DBSCAN_pred_clusters)[1:,1:]
    print("         Bin and Cluster Matrix for dbscan: ")
    print(DBSCAN_confustion_matrix)

    print("         Caluculating K-means SSE")
    DBSCAN_SSE = calculate_SSE(meal_feature_normalized_df, DBSCAN_pred_clusters)

    print("         Caluculating K-means entropy")
    DBSCAN_entropy = calculate_entropy(DBSCAN_confustion_matrix)

    print("         Caluculating K-means purity")
    DBSCAN_purity = calculate_purity(DBSCAN_confustion_matrix)

    result = [kmeans_SSE,DBSCAN_SSE,kmeans_entropy,DBSCAN_entropy,kmeans_purity,DBSCAN_purity]
    print("     Result: ",result)

    print("     Wrtiting data to Result.csv")
    result_df = pd.DataFrame([result])
    result_df.to_csv('Result.csv', index = False, header=False)

    print("Execution Completed")



