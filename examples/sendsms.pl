#! /usr/bin/perl

require LWP::UserAgent;
require HTTP::Cookies;

use strict;

# Include the username/password details for the test account

#require '/usr/local/sakaiconfig/loadtestauth.pl';
#(my $username, my $password) = getSakaiAuth();

#(my $username, my $password) = ("admin", "admin");
#(my $username, my $password) = ("student0001", "sakai");
(my $username, my $password) = ("instructor", "sakai");

# Set the server domain and tool placement URL for T&Q

my $host = "http://localhost:8080";
my $rest = "/direct/sms-task";

my $siteid = "b20dd77f-77a2-45f0-8a22-54161d23beda";

my $ua= LWP::UserAgent->new;
$ua->timeout(10);
$ua->env_proxy;

$ua->cookie_jar({ file => "$ENV{HOME}/.cookies.txt" });

# Login
print "Logging in as $username / $password\n";

my $response = $ua->post("$host/portal/xlogin", 
    [ eid => $username,
      pw  => $password,
      submit => 'Login' ]);

my $auth = 0;

if (($response->is_redirect) && ($response->header("Location") eq "$host/portal")) {
  $auth = 1;
  print "Auth OK!\n";
} else {
  print $response->content;
}

die "Not authed\n" if !$auth;

# print "Cookies: " . $ua->cookie_jar->as_string;

my $deliverylist = "/site/$siteid/role/Instructor";

$response = $ua->post("$host/$rest/new", [
	dateToSend => '2009-05-19T20:32:00.000+0200',
	deliveryEntityList => $deliverylist,
	messageBody => 'hello!',
	sakaiSiteId => $siteid ]);

print "Response code: " . $response->status_line . "\n";
print "Response content: " . $response->content . "\n";  # or whatever

