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

## Get user's email address

sub getUserEmail($$$$) {

    my $ua = shift;
    my $host = shift;
    my $userEid = shift;
    
    $response = $ua->get("$host/direct/user/$userEid.json");

    ## Return the email address if successful, otherwise empty string
       
    if ($debug) {
        print "getUserEmail status: " . $response->status_line . "\n";
    }
    
    if ($response->code ne "200") {
        return "";
    }
    
    ## Parse the JSON 

    my $json = new JSON; 
    my $json_text = $json->decode($response->content);
 
    return $json_text->{email};
}

return 1;