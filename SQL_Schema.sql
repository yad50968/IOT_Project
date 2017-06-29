CREATE TABLE `Server_sensor` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `DeviceId` varchar(192) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `TimeStamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DataType` int(11) NOT NULL,
  `Value` double NOT NULL DEFAULT '0',
  `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `reportid` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=179 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `Server_User` (
  `username` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `Ku` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `token` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `User_Data` (
  `UserID` varchar(25) COLLATE utf8_unicode_ci NOT NULL,
  `Ku` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `XPV` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `UKP` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IDX` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `RES` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  `token` varchar(256) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`UserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE `device_value` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `DeviceId` varchar(192) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `TimeStamp` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DataType` int(11) NOT NULL,
  `Value` double NOT NULL DEFAULT '0',
  `name` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  `reportid` varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
