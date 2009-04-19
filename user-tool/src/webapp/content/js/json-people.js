var var_getEveryoneInSite_participants = {"entityPrefix": "membership", "membership_collection": [
{
  "id": "f3375d4c-5b2a-4072-9c7f-69f0cf0347bb::site:38429753-7acb-41cb-a0cb-9b8cabe69414",
  "lastLoginTime": 1239976037300,
  "locationReference": "\/site\/38429753-7acb-41cb-a0cb-9b8cabe69414",
  "memberRole": "Lecturer",
  "userDisplayId": "1cygfiuygiu@gvsjdjpk.wfwf",
  "userDisplayName": "1cygfiuygiu@gvsjdjpk.wfwf",
  "userEid": "1cygfiuygiu@gvsjdjpk.wfwf",
  "userEmail": "1cygfiuygiu@gvsjdjpk.wfwf",
  "userId": "f3375d4c-5b2a-4072-9c7f-69f0cf0347bb",
  "userSortName": "1cygfiuygiu@gvsjdjpk.wfwf",
  "active": true,
  "provided": false,
  "entityReference": "\/membership\/f3375d4c-5b2a-4072-9c7f-69f0cf0347bb::site:38429753-7acb-41cb-a0cb-9b8cabe69414",
  "entityURL": "http:\/\/137.158.97.158:8080\/direct\/membership\/f3375d4c-5b2a-4072-9c7f-69f0cf0347bb::site:38429753-7acb-41cb-a0cb-9b8cabe69414",
  "entityId": "f3375d4c-5b2a-4072-9c7f-69f0cf0347bb::site:38429753-7acb-41cb-a0cb-9b8cabe69414",
  "entityTitle": "1cygfiuygiu@gvsjdjpk.wfwf"
},{
  "id": "b42beb87-71fe-4514-94eb-d2edce94fe22::site:38429753-7acb-41cb-a0cb-9b8cabe69414",
  "lastLoginTime": 1239976037242,
  "locationReference": "\/site\/38429753-7acb-41cb-a0cb-9b8cabe69414",
  "memberRole": "Support Staff",
  "userDisplayId": "1jhbdsjrgjiu@qwe.er",
  "userDisplayName": "1jhbdsjrgjiu@qwe.er",
  "userEid": "1jhbdsjrgjiu@qwe.er",
  "userEmail": "1jhbdsjrgjiu@qwe.er",
  "userId": "b42beb87-71fe-4514-94eb-d2edce94fe22",
  "userSortName": "1jhbdsjrgjiu@qwe.er",
  "active": true,
  "provided": false,
  "entityReference": "\/membership\/b42beb87-71fe-4514-94eb-d2edce94fe22::site:38429753-7acb-41cb-a0cb-9b8cabe69414",
  "entityURL": "http:\/\/137.158.97.158:8080\/direct\/membership\/b42beb87-71fe-4514-94eb-d2edce94fe22::site:38429753-7acb-41cb-a0cb-9b8cabe69414",
  "entityId": "b42beb87-71fe-4514-94eb-d2edce94fe22::site:38429753-7acb-41cb-a0cb-9b8cabe69414",
  "entityTitle": "1jhbdsjrgjiu@qwe.er"
}
]}

//var tempParticipants = $.getJSON( '/direct/membership/site/' + $('input[name=sakaiSiteId]').val() + '.json' , function(data) {var_getEveryoneInSite_participants = data});
 //var_getEveryoneInSite_participants = tempParticipants;
 $.each(var_getEveryoneInSite_participants.membership_collection, function(i, item) {
 console.log(item.userDisplayName +" --- "+ item.userId);
 });