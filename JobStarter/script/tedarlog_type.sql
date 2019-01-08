SELECT [TypeId]
      ,[TypeCode]
      ,[TypeDescription]
      ,[ActiveFlag]
  FROM [TEDArLog].[dbo].[Type]
 WHERE [TEDArLog].[dbo].[Type].[TypeCode] not like 'CANC%'