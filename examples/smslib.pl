#! /usr/bin/perl

use HTTP::Request::Common qw(PUT POST DELETE);
use JSON;
use Data::Dumper;

## SMS Library functions

my $debug = 1;

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

## Get account ID for a given site ID. Return the new account ID if successful, otherwise empty string

sub getAccountIdForSite($$$) {
    my $ua = shift;
    my $host = shift;
    my $siteId = shift;
    
    $response = $ua->get("$host/direct/sms-account.json?siteId=$siteId");

    if ($debug) {
        print "status: " . $response->status_line . "\n";
    }
    
    if ($response->code ne "200") {
        return "";
    }
    
    ## Parse the JSON 

    my $json = new JSON; 
    my $json_text = $json->decode($response->content);
 
    my $id = "";   
    for my $account ( @{$json_text->{"sms-account_collection"}} ) {
        $id = $account->{id};
    }
    
    return $id;
}

## Create a new account

sub createAccount($$$$) {

    my $ua = shift;
    my $host = shift;
    my $accountName = shift;
    my $siteId = shift;
    
    $response = $ua->post("$host/direct/sms-account/new", [
        accountName => $accountName,
        sakaiSiteId => $siteId ]);

    ## Return the new account ID if successful, otherwise empty string
       
    if ($debug) {
        print "status: " . $response->status_line . "\n";
    }
    
    if ($response->code eq "201") {
        return $response->content;
    } else {
        return "";
    }
}

## Delete account

sub deleteAccount($$$$) {

    my $ua = shift;
    my $host = shift;
    my $accountId = shift;
    
    $response = $ua->request(DELETE "$host/direct/sms-account/$accountId/delete");

    ## Return the new account ID if successful, otherwise empty string
       
    if ($debug) {
        print "status: " . $response->status_line . "\n";
    }
    
    return ($response->code eq "204");
}


## Credit an account

sub creditAccount($$$) {

    my $ua = shift;
    my $host = shift;
    my $accountId = shift;
    my $credits = shift;
    
    $response = $ua->post("$host/direct/sms-account/new", [
        accountName => $accountName,
        sakaiSiteId => $siteid ]);

    $response = $ua->request(POST "$host/direct/sms-account/$accountId/credit", 
        Content => [credits=>$credits]);

    ## Return the new account balance if successful, otherwise empty string
    
    if ($debug) {
        print "status: " . $response->status_line . "\n";
    }
    
    if ($response->code eq "200") {
        return $response->content;
    } else {
        return "";
    }

}

return 1;