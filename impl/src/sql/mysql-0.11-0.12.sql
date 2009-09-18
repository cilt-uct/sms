-- Conversion script from tag 0.11 to tag 0.12

alter table SMS_CONFIG modify CREDIT_COST double not null;

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

