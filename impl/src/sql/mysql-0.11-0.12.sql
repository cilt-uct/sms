-- Conversion script from tag 0.11 to tag 0.12

update SMS_CONFIG SET OVERDRAFT_LIMIT=0 WHERE OVERDRAFT_LIMIT IS NULL;
update SMS_CONFIG SET CREDIT_COST=0 WHERE CREDIT_COST IS NULL;

alter table SMS_CONFIG modify CREDIT_COST double not null;
alter table SMS_CONFIG modify OVERDRAFT_LIMIT double not null;

update SMS_ACCOUNT SET OVERDRAFT_LIMIT=0 WHERE OVERDRAFT_LIMIT IS NULL;
alter table SMS_ACCOUNT MODIFY OVERDRAFT_LIMIT double not null;
alter table SMS_ACCOUNT MODIFY CREDITS double not null;

alter table SMS_TRANSACTION MODIFY TRANSACTION_CREDITS double not null;
alter table SMS_TRANSACTION MODIFY CREDIT_BALANCE double not null;
alter table SMS_TRANSACTION MODIFY SMS_TASK_ID bigint default null;

alter table SMS_MESSAGE ADD CREDITS double not null;

ALTER table SMS_TASK MODIFY CREDIT_ESTIMATE double not null;
ALTER table SMS_TASK drop COST_ESTIMATE;
ALTER table SMS_TASK add CREDITS_ACTUAL double not null;
ALTER table SMS_TASK MODIFY BILLED_CREDITS double not null;

update SMS_TASK SET SAKAI_TOOL_ID='sakai.sms.user' where SAKAI_TOOL_ID is null and MESSAGE_TYPE_ID=0;
