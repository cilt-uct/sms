#! /usr/bin/perl

use HTTP::Request::Common qw(PUT POST DELETE);
use JSON;
use Data::Dumper;

## Sakai Core EB Library functions

my $debug = 0;

## Login to Sakai server. Return true if successful, otherwise false.

sub loginToSakai($$$$) {
  my $ua = shift;
  my $host = shift;
  my $username = shift;
  my $password = shift;
 
  my $response = $ua->post("$host/portal/xlogin", 
    [ eid => $username,
      pw  => $password,
      submit => 'Login' ]);

  return (($response->is_redirect) && ($response->header("Location") eq "$host/portal")); 
}

## Get site title. Return the title if successful, otherwise empty string

sub getSiteName($$$) {
    my $ua = shift;
    my $host = shift;
    my $siteId = shift;
    
    $response = $ua->get("$host/direct/site/$siteId.json");

    if ($debug) {
        print "getSiteName status: " . $response->status_line . "\n";
    }
    
    if ($response->code ne "200") {
        return "";
    }
    
    ## Parse the JSON 

    my $json = new JSON; 
    my $json_text = $json->decode($response->content);
 
    return $json_text->{title};
}

## Get user info given an eid or email address. Return eid, email

sub getUser($$$) {

    my $ua = shift;
    my $host = shift;
    my $userInfo = shift;

    my $try_email = isValidEmailSyntax($userInfo);
  
    # Either an eid or it could be a guest user where the eid looks like an email addr 
    (my $id, my $email) = getUserByEid($ua, $host, $userInfo);

    if (($id eq "") && $try_email) {
	($id, $eid) = getUserByEmail($ua, $host, $userInfo);
	return ($eid, $userInfo);
    } else {
	return ($userInfo, $email);
    }
}

## Get user's id and email address. Returns ID and email

sub getUserByEid($$$) {

    my $ua = shift;
    my $host = shift;
    my $userEid = shift;
    
    $response = $ua->get("$host/direct/user/$userEid.json");

    ## Return the userid and email address if successful, otherwise empty string
       
    if ($debug) {
        print "getUserEmail status: " . $response->status_line . "\n";
    }
    
    if ($response->code ne "200") {
        return ("", "");
    }
    
    ## Parse the JSON 

    my $json = new JSON; 
    my $json_text = $json->decode($response->content);
 
    return ($json_text->{id}, $json_text->{email});
}

## Get user's id and email address

sub getUserNameEmail($$$) {

    my $ua = shift;
    my $host = shift;
    my $userid = shift;

    $response = $ua->get("$host/direct/user/$userid.json");

    ## Return the userid and email address if successful, otherwise empty string

    if ($debug) {
        print "getUserEmail status: " . $response->status_line . "\n";
    }

    if ($response->code ne "200") {
        return ("", "");
    }

    ## Parse the JSON

    my $json = new JSON;
    my $json_text = $json->decode($response->content);

    return ($json_text->{email}, $json_text->{displayName});
}

## Get user's eid given email address, if the email address is unique

sub getUserByEmail($$$) {

    my $ua = shift;
    my $host = shift;
    my $userEmail = shift;
    
    $response = $ua->get("$host/direct/user.json?email=$userEmail");

    ## Return the id and eid if successful and the email address is unique, otherwise empty string
       
    if ($debug) {
        print "getUserEidByEmail status: " . $response->status_line . "\n";
    }
    
    ## Parse the JSON 

    my $json = new JSON; 
    my $json_text = $json->decode($response->content);

    ## Found and unique ?
   
    my $rcount = @{$json_text->{user_collection}};
    
    if ($rcount == 1) {
	return ($json_text->{user_collection}[0]->{id}, $json_text->{user_collection}[0]->{eid});
    }

    ## Not found or not unique

    return ("", "");
}

## Does it look like an email address?

sub isValidEmailSyntax($) {
  my $addr = shift;

  ## Simple regexp from http://www.webmasterworld.com/forum13/251.htm
  return ($addr =~ /^(\w|\-|\_|\.)+\@((\w|\-|\_)+\.)+[a-zA-Z]{2,}$/);
}

return 1;
