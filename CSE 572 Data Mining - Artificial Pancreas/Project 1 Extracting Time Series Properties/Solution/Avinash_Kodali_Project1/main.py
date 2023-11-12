import pandas as pd
import numpy as np

#Read csv datafiles and save in dataframes 
cgm_df=pd.read_csv("CGMData.csv",usecols=["Date","Time","Sensor Glucose (mg/dL)"])
insulin_df=pd.read_csv("InsulinData.csv",usecols=["Date","Time","Alarm"])

# Create a new column date_time_stamp and make it as an index.
cgm_df["date_time_stamp"] = pd.to_datetime(cgm_df["Date"] + " " + cgm_df["Time"])
cgm_date_time_index_df=cgm_df.set_index("date_time_stamp")

insulin_df["date_time_stamp"]=pd.to_datetime(insulin_df["Date"] + ' ' + insulin_df["Time"])
insulin_date_time_index_df = insulin_df.set_index("date_time_stamp")

# Clean the dataframes by removing the null values in Sensor Glucose (mg/dL) column
cgm_without_null_values_df = cgm_date_time_index_df.dropna(subset=["Sensor Glucose (mg/dL)"])

# Remove dates whose entries are less than 80% of 288
dates_with_entries_atleast_80_percent_of_288=cgm_without_null_values_df.groupby('Date')['Sensor Glucose (mg/dL)'].count().where(lambda x:x>=0.8*288).dropna().index.tolist()
cgm_without_null_values_df=cgm_without_null_values_df.loc[cgm_without_null_values_df["Date"].isin(dates_with_entries_atleast_80_percent_of_288)]

# Extract the date and time when the mode changes from manual to auto
auto_mode_start_date_time_stamp=insulin_df.sort_values(by='date_time_stamp',ascending=True).loc[insulin_df['Alarm']=='AUTO MODE ACTIVE PLGM OFF'].iloc[0]['date_time_stamp']
print("Auto mode start date: ",auto_mode_start_date_time_stamp)

# Split the cgm data into manual and auto mode
cgm_auto_df = cgm_without_null_values_df.loc[cgm_without_null_values_df.index>=auto_mode_start_date_time_stamp]
cgm_manual_df = cgm_without_null_values_df.loc[cgm_without_null_values_df.index<auto_mode_start_date_time_stamp]

# Calculation for auto mode dataframe

# percentage time in hyperglycemia (CGM > 180 mg/dL) - daytime, overnight, wholeday
auto_mode_daytime_percent_time_in_hyperglycemia=(cgm_auto_df.between_time('06:00:00','23:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']>180].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_overnight_percent_time_in_hyperglycemia=(cgm_auto_df.between_time('0:00:00','05:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']>180].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_wholeday_percent_time_in_hyperglycemia=(cgm_auto_df.between_time('0:00:00','23:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']>180].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage of time in hyperglycemia critical (CGM > 250 mg/dL) - wholeday, daytime, overnight
auto_mode_daytime_percent_time_in_hyperglycemia_critical=(cgm_auto_df.between_time('06:00:00','23:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']>250].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_overnight_percent_time_in_hyperglycemia_critical=(cgm_auto_df.between_time('0:00:00','05:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']>250].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_wholeday_percent_time_in_hyperglycemia_critical=(cgm_auto_df.between_time('0:00:00','23:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']>250].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage time in range (CGM >= 70 mg/dL and CGM <= 180 mg/dL) - wholeday, daytime, overnight
auto_mode_daytime_percent_time_in_range=(cgm_auto_df.between_time('06:00:00','23:59:59').loc[(cgm_auto_df['Sensor Glucose (mg/dL)']>=70) & (cgm_auto_df['Sensor Glucose (mg/dL)']<=180)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_overnight_percent_time_in_range=(cgm_auto_df.between_time('0:00:00','05:59:59').loc[(cgm_auto_df['Sensor Glucose (mg/dL)']>=70) & (cgm_auto_df['Sensor Glucose (mg/dL)']<=180)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_wholeday_percent_time_in_range=(cgm_auto_df.between_time('0:00:00','23:59:59').loc[(cgm_auto_df['Sensor Glucose (mg/dL)']>=70) & (cgm_auto_df['Sensor Glucose (mg/dL)']<=180)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage time in range secondary (CGM >= 70 mg/dL and CGM <= 150 mg/dL) - wholeday, daytime, overnight
auto_mode_daytime_percent_time_in_range_secondary=(cgm_auto_df.between_time('06:00:00','23:59:59').loc[(cgm_auto_df['Sensor Glucose (mg/dL)']>=70) & (cgm_auto_df['Sensor Glucose (mg/dL)']<=150)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_overnight_percent_time_in_range_secondary=(cgm_auto_df.between_time('0:00:00','05:59:59').loc[(cgm_auto_df['Sensor Glucose (mg/dL)']>=70) & (cgm_auto_df['Sensor Glucose (mg/dL)']<=150)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_wholeday_percent_time_in_range_secondary=(cgm_auto_df.between_time('0:00:00','23:59:59').loc[(cgm_auto_df['Sensor Glucose (mg/dL)']>=70) & (cgm_auto_df['Sensor Glucose (mg/dL)']<=150)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage time in hypoglycemia level 1 (CGM < 70 mg/dL) - wholeday, daytime, overnight
auto_mode_daytime_percent_time_in_hypoglycemia_level_1=(cgm_auto_df.between_time('06:00:00','23:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']<70].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_overnight_percent_time_in_hypoglycemia_level_1=(cgm_auto_df.between_time('0:00:00','05:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']<70].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_wholeday_percent_time_in_hypoglycemia_level_1=(cgm_auto_df.between_time('0:00:00','23:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']<70].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage time in hypoglycemia level 2 (CGM < 54 mg/dL)- wholeday, daytime, overnight
auto_mode_daytime_percent_time_in_hypoglycemia_level_2=(cgm_auto_df.between_time('06:00:00','23:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']<54].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_overnight_percent_time_in_hypoglycemia_level_2=(cgm_auto_df.between_time('0:00:00','05:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']<54].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
auto_mode_wholeday_percent_time_in_hypoglycemia_level_2=(cgm_auto_df.between_time('0:00:00','23:59:59').loc[cgm_auto_df['Sensor Glucose (mg/dL)']<54].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# Calculation for manual mode dataframe

# percentage time in hyperglycemia (CGM > 180 mg/dL) - daytime, overnight, wholeday
manual_mode_daytime_percent_time_in_hyperglycemia=(cgm_manual_df.between_time('06:00:00','23:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']>180].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_overnight_percent_time_in_hyperglycemia=(cgm_manual_df.between_time('0:00:00','05:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']>180].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_wholeday_percent_time_in_hyperglycemia=(cgm_manual_df.between_time('0:00:00','23:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']>180].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage of time in hyperglycemia critical (CGM > 250 mg/dL) - wholeday, daytime, overnight
manual_mode_daytime_percent_time_in_hyperglycemia_critical=(cgm_manual_df.between_time('06:00:00','23:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']>250].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_overnight_percent_time_in_hyperglycemia_critical=(cgm_manual_df.between_time('0:00:00','05:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']>250].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_wholeday_percent_time_in_hyperglycemia_critical=(cgm_manual_df.between_time('0:00:00','23:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']>250].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage time in range (CGM >= 70 mg/dL and CGM <= 180 mg/dL) - wholeday, daytime, overnight
manual_mode_daytime_percent_time_in_range=(cgm_manual_df.between_time('06:00:00','23:59:59').loc[(cgm_manual_df['Sensor Glucose (mg/dL)']>=70) & (cgm_manual_df['Sensor Glucose (mg/dL)']<=180)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_overnight_percent_time_in_range=(cgm_manual_df.between_time('0:00:00','05:59:59').loc[(cgm_manual_df['Sensor Glucose (mg/dL)']>=70) & (cgm_manual_df['Sensor Glucose (mg/dL)']<=180)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_wholeday_percent_time_in_range=(cgm_manual_df.between_time('0:00:00','23:59:59').loc[(cgm_manual_df['Sensor Glucose (mg/dL)']>=70) & (cgm_manual_df['Sensor Glucose (mg/dL)']<=180)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage time in range secondary (CGM >= 70 mg/dL and CGM <= 150 mg/dL) - wholeday, daytime, overnight
manual_mode_daytime_percent_time_in_range_secondary=(cgm_manual_df.between_time('06:00:00','23:59:59').loc[(cgm_manual_df['Sensor Glucose (mg/dL)']>=70) & (cgm_manual_df['Sensor Glucose (mg/dL)']<=150)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_overnight_percent_time_in_range_secondary=(cgm_manual_df.between_time('0:00:00','05:59:59').loc[(cgm_manual_df['Sensor Glucose (mg/dL)']>=70) & (cgm_manual_df['Sensor Glucose (mg/dL)']<=150)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_wholeday_percent_time_in_range_secondary=(cgm_manual_df.between_time('0:00:00','23:59:59').loc[(cgm_manual_df['Sensor Glucose (mg/dL)']>=70) & (cgm_manual_df['Sensor Glucose (mg/dL)']<=150)].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage time in hypoglycemia level 1 (CGM < 70 mg/dL) - wholeday, daytime, overnight
manual_mode_daytime_percent_time_in_hypoglycemia_level_1=(cgm_manual_df.between_time('06:00:00','23:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']<70].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_overnight_percent_time_in_hypoglycemia_level_1=(cgm_manual_df.between_time('0:00:00','05:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']<70].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_wholeday_percent_time_in_hypoglycemia_level_1=(cgm_manual_df.between_time('0:00:00','23:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']<70].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

# percentage time in hypoglycemia level 2 (CGM < 54 mg/dL)- wholeday, daytime, overnight
manual_mode_daytime_percent_time_in_hypoglycemia_level_2=(cgm_manual_df.between_time('06:00:00','23:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']<54].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_overnight_percent_time_in_hypoglycemia_level_2=(cgm_manual_df.between_time('0:00:00','05:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']<54].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)
manual_mode_wholeday_percent_time_in_hypoglycemia_level_2=(cgm_manual_df.between_time('0:00:00','23:59:59').loc[cgm_manual_df['Sensor Glucose (mg/dL)']<54].groupby('Date')['Sensor Glucose (mg/dL)'].count()/288*100)

#Find mean and store in result dataframe, finally save it to csv

results_df = pd.DataFrame({"Overnight Percentage time in hyperglycemia (CGM > 180 mg/dL)":[ manual_mode_overnight_percent_time_in_hyperglycemia.mean(axis=0),auto_mode_overnight_percent_time_in_hyperglycemia.mean(axis=0)],
                           "Overnight percentage of time in hyperglycemia critical (CGM > 250 mg/dL)":[manual_mode_overnight_percent_time_in_hyperglycemia_critical.mean(axis=0),auto_mode_overnight_percent_time_in_hyperglycemia_critical.mean(axis=0)],
                           "Overnight percentage time in range (CGM >= 70 mg/dL and CGM <= 180 mg/dL)":[manual_mode_overnight_percent_time_in_range.mean(axis=0),auto_mode_overnight_percent_time_in_range.mean(axis=0)],
                           "Overnight percentage time in range secondary (CGM >= 70 mg/dL and CGM <= 150 mg/dL)":[manual_mode_overnight_percent_time_in_range_secondary.mean(axis=0),auto_mode_overnight_percent_time_in_range_secondary.mean(axis=0)],
                           "Overnight percentage time in hypoglycemia level 1 (CGM < 70 mg/dL)":[manual_mode_overnight_percent_time_in_hypoglycemia_level_1.mean(axis=0),auto_mode_overnight_percent_time_in_hypoglycemia_level_1.mean(axis=0)],
                           "Overnight percentage time in hypoglycemia level 2 (CGM < 54 mg/dL)":[manual_mode_overnight_percent_time_in_hypoglycemia_level_2.mean(axis=0),auto_mode_overnight_percent_time_in_hypoglycemia_level_2.mean(axis=0)],
                           "Daytime Percentage time in hyperglycemia (CGM > 180 mg/dL)":[manual_mode_daytime_percent_time_in_hyperglycemia.mean(axis=0),auto_mode_daytime_percent_time_in_hyperglycemia.mean(axis=0)],
                           "Daytime percentage of time in hyperglycemia critical (CGM > 250 mg/dL)":[manual_mode_daytime_percent_time_in_hyperglycemia_critical.mean(axis=0),auto_mode_daytime_percent_time_in_hyperglycemia_critical.mean(axis=0)],
                           "Daytime percentage time in range (CGM >= 70 mg/dL and CGM <= 180 mg/dL)":[manual_mode_daytime_percent_time_in_range.mean(axis=0),auto_mode_daytime_percent_time_in_range.mean(axis=0)],
                           "Daytime percentage time in range secondary (CGM >= 70 mg/dL and CGM <= 150 mg/dL)":[manual_mode_daytime_percent_time_in_range_secondary.mean(axis=0),auto_mode_daytime_percent_time_in_range_secondary.mean(axis=0)],
                           "Daytime percentage time in hypoglycemia level 1 (CGM < 70 mg/dL)":[manual_mode_daytime_percent_time_in_hypoglycemia_level_1.mean(axis=0),auto_mode_daytime_percent_time_in_hypoglycemia_level_1.mean(axis=0)],
                           "Daytime percentage time in hypoglycemia level 2 (CGM < 54 mg/dL)":[manual_mode_daytime_percent_time_in_hypoglycemia_level_2.mean(axis=0),auto_mode_daytime_percent_time_in_hypoglycemia_level_2.mean(axis=0)],
                           "Whole Day Percentage time in hyperglycemia (CGM > 180 mg/dL)":[manual_mode_wholeday_percent_time_in_hyperglycemia.mean(axis=0),auto_mode_wholeday_percent_time_in_hyperglycemia.mean(axis=0)],
                           "Whole day percentage of time in hyperglycemia critical (CGM > 250 mg/dL)":[manual_mode_wholeday_percent_time_in_hyperglycemia_critical.mean(axis=0),auto_mode_wholeday_percent_time_in_hyperglycemia_critical.mean(axis=0)],
                           "Whole day percentage time in range (CGM >= 70 mg/dL and CGM <= 180 mg/dL)":[manual_mode_wholeday_percent_time_in_range.mean(axis=0),auto_mode_wholeday_percent_time_in_range.mean(axis=0)],
                           "Whole day percentage time in range secondary (CGM >= 70 mg/dL and CGM <= 150 mg/dL)":[manual_mode_wholeday_percent_time_in_range_secondary.mean(axis=0),auto_mode_wholeday_percent_time_in_range_secondary.mean(axis=0)],
                           "Whole day percentage time in hypoglycemia level 1 (CGM < 70 mg/dL)":[manual_mode_wholeday_percent_time_in_hypoglycemia_level_1.mean(axis=0),auto_mode_wholeday_percent_time_in_hypoglycemia_level_1.mean(axis=0)],
                           "Whole Day percentage time in hypoglycemia level 2 (CGM < 54 mg/dL)":[manual_mode_wholeday_percent_time_in_hypoglycemia_level_2.mean(axis=0),auto_mode_wholeday_percent_time_in_hypoglycemia_level_2.mean(axis=0)]},
                           index=['manual_mode','auto_mode'])

results_df=results_df.fillna(0)

results_df.to_csv('Results.csv',header=False,index=False)

















