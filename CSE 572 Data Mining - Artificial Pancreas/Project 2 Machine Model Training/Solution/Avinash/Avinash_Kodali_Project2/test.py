import pandas as pd
import numpy as np
from datetime import timedelta
from scipy.fftpack import fft, ifft,rfft
from sklearn.utils import shuffle
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
from joblib import dump, load

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
    
    print('Started executing test.py ...')
    # Read test csv
    print('     Reading test file')
    test_df=pd.read_csv('test.csv',header=None)

    # Create no meal feature matrix
    print('     creating no meal feature matrix')
    no_meal_feature_matrix=createnomealfeaturematrix(test_df)

    # Predict the results
    print('     Predicting the output and saving it into Result file')
    with open('DecisionTreeClassifier.pickle', 'rb') as model:
        clf = load(model)
        y_test_pred = clf.predict(no_meal_feature_matrix)    
        pd.DataFrame(y_test_pred).to_csv('Result.csv',index=False,header=False)
        model.close()
    
    print('Execution Completed.')




