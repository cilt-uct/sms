 -- ***********************************************************************************
 -- mysql_database_indexes.sql.java
 -- Copyright (c) 2008 Sakai Project/Sakai Foundation
 -- 
 -- Licensed under the Educational Community License, Version 2.0 (the "License"); 
 -- you may not use this file except in compliance with the License. 
 -- You may obtain a copy of the License at
 -- 
 --      http://www.osedu.org/licenses/ECL-2.0
 -- 
 -- Unless required by applicable law or agreed to in writing, software 
 -- distributed under the License is distributed on an "AS IS" BASIS, 
 -- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 -- See the License for the specific language governing permissions and 
 -- limitations under the License.
 --
 -- **********************************************************************************/

 -- Creating indexes on SMS_TASK
CREATE INDEX DATE_TO_SEND ON SMS_TASK(DATE_TO_SEND);
CREATE INDEX MESSAGE_TYPE_ID ON SMS_TASK(MESSAGE_TYPE_ID);
CREATE INDEX STATUS_CODE ON SMS_TASK(STATUS_CODE);
CREATE INDEX MESSAGES_PROCESSED ON SMS_TASK(MESSAGES_PROCESSED);
CREATE INDEX GROUP_SIZE_ACTUAL ON SMS_TASK(GROUP_SIZE_ACTUAL);

-- Creating indexes on SMS_MESSAGE
CREATE INDEX STATUS_CODE ON SMS_MESSAGE(STATUS_CODE);