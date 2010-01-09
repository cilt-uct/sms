 <?php
/*
Defines the authentification details for sakai details as $user and $password
*/
require("auth.php");

$host="https://localgist:8080";


$url=$host."direct/sms-task/new";

//Teh short code number that will handle responses
$return_number="00000";

// The id of the site that contains the sms tool
$siteId="!admin";


//the sms account Id to bill
$accountId=51;

/** date to send the sms
*/
$dateToSend="2010-01-09T08:30:00";

/*
* The file containing the offers
* format is: userEid, degree, mobile number
*/
$fin = fopen("offers.csv","r") or die("cant open offers file");
$i=0;
while (!feof($fin)) {
     $line = fgetcsv($fin);
     $i++;
   
    $campusId=$line[0];
    $degreeCode=$line[1];
    $number=$line[2];
    if(strlen($number)==0){
	print "number is empyt! \n";
	$i = $i -1;
	continue;

    }
    
    $message="Student No.$campusId: UCT Commerce has made you an offer for $degreeCode for 2010. Sms the words: offer COM $campusId accept OR decline to $return_number";

    print "\n$message \n";
    if (strlen($message) > 160) die ("Message longer than 160 chars for
    CampusId \n");
    
    print "Message is ".strlen($message)." chars long \n";
    //the data fields
    $data["attemptCount"]=0;
    $data["dateToSend"]=$dateToSend;
    print "Date to send ".$data["dateToSend"]."\n";
    $data["deliveryMobileNumbersSet"] = $number;
    $data["groupSizeEstimate"]=1;
    $data["messageBody"]=$message;
    $data["messageTypeId"]=0;
    $data["sakaiSiteId"]=$siteId;
    $data["sakaiToolId"]="sakai.sms.user";
    $data["senderUserId"]="dhorwitz";
    $data["senderUserName"]="01302922";
    $data["smsAccountId"]=$accountId;
    $data["statusCode"]="P";
    $data["creditEstimate"]="1";

    $ret = post_it($data,$url);
    print "$ret \n";

   if (!is_numeric($ret)) die("Non numeric response recieved\n");
}


fclose($fin);
print "Queued $i tasks\n";


function post_it($datastream, $url) {
	global $username, $password;

$debug=false;
if ($debug) {
 echo "posting file... to $url <br>";
foreach ($datastream as $key=>$value) {
echo "$key: $value <br>";
}
}
// init curl handle

  $ch = curl_init($url) or die("couldn't init curl");
      curl_setopt($ch, CURLOPT_URL, $url);
      curl_setopt($ch, CURLOPT_VERBOSE, 0);
      curl_setopt($ch, CURLOPT_HEADER, 0);
      curl_setopt($ch, CURLOPT_FOLLOWLOCATION,1);
      curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
      curl_setopt($ch, CURLOPT_POSTFIELDS, $datastream);
      curl_setopt($ch, CURLOPT_HTTPPROXYTUNNEL, false);
      curl_setopt($ch, CURLOPT_SSL_VERIFYHOST,  2);
      curl_setopt($ch, CURLOPT_RETURNTRANSFER,1);
      // this line makes it work under https
      curl_setopt($ch, CURLOPT_SSL_VERIFYPEER,0);
      curl_setopt($ch, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);
      curl_setopt($ch, CURLOPT_USERPWD, "$username:$password");
      print "loging in as $username \n";
	  // perform post
	  $rr=curl_exec($ch);
	  curl_close($ch);
	  
	      #echo $rr;
	      if ($rr) {
		return $rr;
	      } else {
	      //echo "nothing back!";
		echo "<br>error: ".curl_errno($ch)."---".curl_error($ch)."<br>";
		return $rr;
	      }

} 





?>
