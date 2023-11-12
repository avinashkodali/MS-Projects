import pandas as pd
import numpy as np
from datetime import timedelta
from scipy.fftpack import fft, ifft,rfft
from sklearn.utils import shuffle
from sklearn.tree import DecisionTreeClassifier
from sklearn.model_selection import KFold, RepeatedKFold
from joblib import dump, load
from sklearn.metrics import accuracy_score, f1_score, precision_score, recall_score

def createmealdata(insulin_df,cgm_df,dateidentifier):
    '''
    This function is used to create a meal data
    '''
    insulin_df=insulin_df.replace(0.0,np.nan).dropna().reset_index(drop=True)
    
    valid_datetimestamps=[]

    for idx,datetimestamp in enumerate(insulin_df['date_time_stamp']):
        try:
            temp=(insulin_df['date_time_stamp'][idx+1]-datetimestamp).seconds / 60.0
            if temp >= 120:
                valid_datetimestamps.append(datetimestamp)
        except KeyError:
            break
    
    meal_list=[]
    if dateidentifier==1:
        for idx,datetimestamp in enumerate(valid_datetimestamps):
            start_time=pd.to_datetime(datetimestamp - timedelta(minutes=30))
            end_time=pd.to_datetime(datetimestamp + timedelta(minutes=120))
            get_date=datetimestamp.date().strftime('%#m/%#d/%Y')
            meal_list.append(cgm_df.loc[cgm_df['Date']==get_date].set_index('date_time_stamp').between_time(start_time=start_time.strftime('%#H:%#M:%#S'),end_time=end_time.strftime('%#H:%#M:%#S'))['Sensor Glucose (mg/dL)'].values.tolist())
        return pd.DataFrame(meal_list)
    else:
        for idx,datetimestamp in enumerate(valid_datetimestamps):
            start_time=pd.to_datetime(datetimestamp - timedelta(minutes=30))
            end_time=pd.to_datetime(datetimestamp + timedelta(minutes=120))
            get_date=datetimestamp.date().strftime('%Y-%m-%d')
            meal_list.append(cgm_df.loc[cgm_df['Date']==get_date].set_index('date_time_stamp').between_time(start_time=start_time.strftime('%H:%M:%S'),end_time=end_time.strftime('%H:%M:%S'))['Sensor Glucose (mg/dL)'].values.tolist())
        return pd.DataFrame(meal_list)

def createnomealdata(insulin_df,cgm_df):
    '''
    This function is used to create a no meal data
    '''
    insulin_df=insulin_df.replace(0.0,np.nan).dropna().reset_index(drop=True)

    valid_datetimestamps=[]
    for idx,date_time_stamp in enumerate(insulin_df['date_time_stamp']):
        try:
            value=(insulin_df['date_time_stamp'][idx+1]-date_time_stamp).seconds//3600
            if value >=4:
                valid_datetimestamps.append(date_time_stamp)
        except KeyError:
            break
            
    no_meal_list=[]
    for idx, date_time_stamp in enumerate(valid_datetimestamps):
        iteration_no_meal_list=1
        try:
            length_of_24_no_meal_list=len(cgm_df.loc[(cgm_df['date_time_stamp']>=valid_datetimestamps[idx]+pd.Timedelta(hours=2))&(cgm_df['date_time_stamp']<valid_datetimestamps[idx+1])])//24
            while (iteration_no_meal_list<=length_of_24_no_meal_list):
                if iteration_no_meal_list==1:
                    no_meal_list.append(cgm_df.loc[(cgm_df['date_time_stamp']>=valid_datetimestamps[idx]+pd.Timedelta(hours=2))&(cgm_df['date_time_stamp']<valid_datetimestamps[idx+1])]['Sensor Glucose (mg/dL)'][:iteration_no_meal_list*24].values.tolist())
                    iteration_no_meal_list+=1
                else:
                    no_meal_list.append(cgm_df.loc[(cgm_df['date_time_stamp']>=valid_datetimestamps[idx]+pd.Timedelta(hours=2))&(cgm_df['date_time_stamp']<valid_datetimestamps[idx+1])]['Sensor Glucose (mg/dL)'][(iteration_no_meal_list-1)*24:iteration_no_meal_list*24].values.tolist())
                    iteration_no_meal_list+=1
        except IndexError:
            break
    return pd.DataFrame(no_meal_list)

def createmealfeaturematrix(meal_data):
    '''
    This function is used to create a feature matrix for meal data
    '''
    index=meal_data.isna().sum(axis=1).replace(0,np.nan).dropna().where(lambda x:x>6).dropna().index 
    meal_df_cleaned=meal_data.drop(meal_data.index[index]).reset_index().drop(columns='index')
    meal_df_cleaned=meal_df_cleaned.interpolate(method='linear',axis=1)
    index_to_drop=meal_df_cleaned.isna().sum(axis=1).replace(0,np.nan).dropna().index
    meal_df_cleaned=meal_df_cleaned.drop(meal_data.index[index_to_drop]).reset_index().drop(columns='index')
    tau_time=abs((meal_df_cleaned.iloc[:,7:].idxmax(axis=1)-6)*5)
    difference_in_glucose_normalized=abs((meal_df_cleaned.iloc[:,7:].max(axis=1)-meal_df_cleaned.iloc[:,6])/(meal_df_cleaned.iloc[:,6]))
    meal_df_cleaned=meal_df_cleaned.dropna().reset_index().drop(columns='index')
    power_first_max=[]
    index_first_max=[]
    power_second_max=[]
    index_second_max=[]
    for i in range(len(meal_df_cleaned)):
        rfft_array=abs(rfft(meal_df_cleaned.iloc[:,0:30].iloc[i].values.tolist())).tolist()
        rfft_array_sorted=abs(rfft(meal_df_cleaned.iloc[:,0:30].iloc[i].values.tolist())).tolist()
        rfft_array_sorted.sort()
        power_first_max.append(rfft_array_sorted[-2])
        power_second_max.append(rfft_array_sorted[-3])
        index_first_max.append(rfft_array.index(rfft_array_sorted[-2]))
        index_second_max.append(rfft_array.index(rfft_array_sorted[-3]))
    meal_feature_matrix=pd.DataFrame()
    meal_feature_matrix['tau_time']=tau_time
    meal_feature_matrix['difference_in_glucose_normalized']=difference_in_glucose_normalized
    meal_feature_matrix['power_first_max']=power_first_max
    meal_feature_matrix['power_second_max']=power_second_max
    meal_feature_matrix['index_first_max']=index_first_max
    meal_feature_matrix['index_second_max']=index_second_max
    tm = 6
    maximum=meal_df_cleaned.iloc[:,7:].idxmax(axis=1)
    first_differential_data=[]
    second_differential_data=[]
    for i in range(len(meal_df_cleaned)):
        first_differential_data.append(np.diff(meal_df_cleaned.iloc[i,tm:(maximum[i]+1)].tolist()).max())
        if(len(meal_df_cleaned.iloc[i,tm:(maximum[i]+1)])>2):
            second_differential_data.append(np.diff(np.diff(meal_df_cleaned.iloc[i,tm:maximum[i]+1].tolist())).max())
        else:
            second_differential_data.append(0)     
    meal_feature_matrix['1stDifferential']=first_differential_data
    meal_feature_matrix['2ndDifferential']=second_differential_data
    return meal_feature_matrix

def createnomealfeaturematrix(non_meal_df):
    '''
    This function is used to create a feature matrix for no meal data
    '''
    index=non_meal_df.isna().sum(axis=1).replace(0,np.nan).dropna().where(lambda x:x>5).dropna().index
    non_meal_df_cleaned=non_meal_df.drop(non_meal_df.index[index]).reset_index().drop(columns='index')
    non_meal_df_cleaned=non_meal_df_cleaned.interpolate(method='linear',axis=1)
    index_to_drop=non_meal_df_cleaned.isna().sum(axis=1).replace(0,np.nan).dropna().index
    non_meal_df_cleaned=non_meal_df_cleaned.drop(non_meal_df_cleaned.index[index_to_drop]).reset_index().drop(columns='index')
    non_meal_feature_matrix=pd.DataFrame()
    tau_time=abs((non_meal_df_cleaned.iloc[:,1:].idxmax(axis=1)-0)*5)
    difference_in_glucose_normalized=abs((non_meal_df_cleaned.iloc[:,1:].max(axis=1)-non_meal_df_cleaned.iloc[:,0])/(non_meal_df_cleaned.iloc[:,0]))
    power_first_max,index_first_max,power_second_max,index_second_max=[],[],[],[]
    for i in range(len(non_meal_df_cleaned)):
        rfft_array=abs(rfft(non_meal_df_cleaned.iloc[:,0:24].iloc[i].values.tolist())).tolist()
        rfft_array_sorted=abs(rfft(non_meal_df_cleaned.iloc[:,0:24].iloc[i].values.tolist())).tolist()
        rfft_array_sorted.sort()
        power_first_max.append(rfft_array_sorted[-2])
        power_second_max.append(rfft_array_sorted[-3])
        index_first_max.append(rfft_array.index(rfft_array_sorted[-2]))
        index_second_max.append(rfft_array.index(rfft_array_sorted[-3]))
    non_meal_feature_matrix['tau_time']=tau_time
    non_meal_feature_matrix['difference_in_glucose_normalized']=difference_in_glucose_normalized
    non_meal_feature_matrix['power_first_max']=power_first_max
    non_meal_feature_matrix['power_second_max']=power_second_max
    non_meal_feature_matrix['index_first_max']=index_first_max
    non_meal_feature_matrix['index_second_max']=index_second_max
    first_differential_data=[]
    second_differential_data=[]
    for i in range(len(non_meal_df_cleaned)):
        first_differential_data.append(np.diff(non_meal_df_cleaned.iloc[:,0:24].iloc[i].tolist()).max())
        second_differential_data.append(np.diff(np.diff(non_meal_df_cleaned.iloc[:,0:24].iloc[i].tolist())).max())
    non_meal_feature_matrix['1stDifferential']=first_differential_data
    non_meal_feature_matrix['2ndDifferential']=second_differential_data
    return non_meal_feature_matrix

if __name__=="__main__":

    print('Started Executing train.py ...')

    #Read Insulin data
    print('     Reading Insulin data')
    insulin_data_df=pd.read_csv('InsulinData.csv',low_memory=False,usecols=['Date','Time','BWZ Carb Input (grams)'])
    insulin_data_df['date_time_stamp']=pd.to_datetime(insulin_data_df['Date'] + ' ' + insulin_data_df['Time'])
    insulin_data_df=insulin_data_df.sort_values(by='date_time_stamp',ascending=True).reset_index(drop=True)

    insulin_patient_df=pd.read_csv('Insulin_patient2.csv',low_memory=False,usecols=['Date','Time','BWZ Carb Input (grams)'])
    insulin_patient_df['date_time_stamp']=pd.to_datetime(insulin_patient_df['Date'] + ' ' + insulin_patient_df['Time'])
    insulin_patient_df=insulin_patient_df.sort_values(by='date_time_stamp',ascending=True).reset_index(drop=True)
    
    #Read CGM data
    print('     Reading CGM data')
    cgm_data_df=pd.read_csv('CGMData.csv',low_memory=False,usecols=['Date','Time','Sensor Glucose (mg/dL)'])
    cgm_data_df['date_time_stamp']=pd.to_datetime(cgm_data_df['Date'] + ' ' + cgm_data_df['Time'])
    cgm_data_df=cgm_data_df.sort_values(by='date_time_stamp',ascending=True).reset_index(drop=True)

    cgm_patient_df=pd.read_csv('CGM_patient2.csv',low_memory=False,usecols=['Date','Time','Sensor Glucose (mg/dL)'])
    cgm_patient_df['date_time_stamp']=pd.to_datetime(cgm_patient_df['Date'] + ' ' + cgm_patient_df['Time'])
    cgm_patient_df=cgm_patient_df.sort_values(by='date_time_stamp',ascending=True).reset_index(drop=True)

    #Create Meal Data
    print('     Creating Meal data')
    meal_data=createmealdata(insulin_data_df,cgm_data_df,1)#.iloc[:,0:30]
    meal_patient=createmealdata(insulin_patient_df,cgm_patient_df,2)#.iloc[:,0:30]

    #Create No Meal Data
    print('     Creating No meal data')
    no_meal_data=createnomealdata(insulin_data_df,cgm_data_df)
    no_meal_patient=createnomealdata(insulin_patient_df,cgm_patient_df)

    #Create meal feature matrix
    print('     Creating meal feature matrix')
    meal_data_feature_matrix=createmealfeaturematrix(meal_data)
    meal_patient_feature_matrix=createmealfeaturematrix(meal_patient)
    meal_feature_matrix=pd.concat([meal_data_feature_matrix,meal_patient_feature_matrix]).reset_index(drop=True)

    #Create No meal feature matrix
    print('     Creating no meal feature matrix')
    non_meal_data_feature_matrix=createnomealfeaturematrix(no_meal_data)
    non_meal_patient_feature_matrix=createnomealfeaturematrix(no_meal_patient)
    non_meal_feature_matrix=pd.concat([non_meal_data_feature_matrix,non_meal_patient_feature_matrix]).reset_index().drop(columns='index')

    #Training Machine Learning Model and perform model evaluation
    print('     Training machine learning model and performing k-fold cross validation')
    meal_feature_matrix['class_label']=1
    non_meal_feature_matrix['class_label']=0
    feature_matrix=pd.concat([meal_feature_matrix,non_meal_feature_matrix]).reset_index(drop=True)
    dataset=shuffle(feature_matrix,random_state=1).reset_index(drop=True)
    kfold = KFold(n_splits=10,shuffle=True,random_state=1)
    feature_data=dataset.drop(columns='class_label')
    accuracy, f1, precision, recall = [], [], [], []
    clf=DecisionTreeClassifier(criterion="entropy")
    for train_index, test_index in kfold.split(feature_data):
        X_train,X_test,y_train,y_test = feature_data.loc[train_index],feature_data.loc[test_index],dataset.class_label.loc[train_index],dataset.class_label.loc[test_index]
        clf.fit(X_train,y_train)
        y_test_pred=clf.predict(X_test)
        accuracy.append(accuracy_score(y_test,y_test_pred))
        f1.append(f1_score(y_test,y_test_pred))
        precision.append(precision_score(y_test,y_test_pred))
        recall.append(recall_score(y_test,y_test_pred))
    print('     Performance Metrics')
    print('         Accuracy score is',np.mean(accuracy)*100)
    print('         F1 Score score is',np.mean(f1)*100)
    print('         Precision score is',np.mean(precision)*100)
    print('         Recall score is',np.mean(recall)*100)

    # Save machine learning model as pickle file
    print('     Saving machine learning model to pickle file')
    X, y= feature_data, dataset['class_label']
    clf.fit(X,y)
    dump(clf, 'DecisionTreeClassifier.pickle')

    print('Execution Completed.')
 
