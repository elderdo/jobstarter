SELECT [PART_KEY]
      ,[SEQUENCE_NUMBER]
      ,[USER_ID]
      ,[GROUP_NAME]
      ,[Action_Group]
      ,[APPROVAL_STEP]
      ,[Date_In_Queue]
      ,[Date_In_Que_Mday]
      ,[SCHEDULED_START_DATE]
      ,[Schedule_Start_Mday]
      ,[ACTUAL_START_DATE]
      ,[Actual_Start_Mday]
      ,[ACTUAL_FINISH_DATE]
      ,[Actual_Finish_Mday]
      ,[Days_In_Signoff]
      ,[Days_In_Single_Sequence]
      ,[ACCEPT_REJECT]
      ,[CURRENT_WORK]
      ,[IN_WORK_FLAG]
      ,[ORIGINAL_SIGNOFF_CYCLE]
      ,[COMMENTS]
      ,[NOTIFICATION_ONLY]
      ,[SEND_EMAIL]
      ,[In_Work_DATE]
      ,[InWork_Mday]
      ,[OrderID]
      ,[PLAN_STATUS]
      ,[PLAN_SEQUENCE]
      ,[RevNumber]
      ,[Signoff_Cycle]
  FROM [TEDTLC].[dbo].[MES_CAPP_ToolOrder_InSignOff]