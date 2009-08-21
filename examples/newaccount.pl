#! /usr/bin/perl

require LWP::UserAgent;
require HTTP::Cookies;

require 'smslib.pl';

use strict;

# Include the username/password details - requires an admin-equivalent account

#require '/usr/local/sakaiconfig/loadtestauth.pl';
#(my $username, my $password) = getSakaiAuth();

(my $username, my $password) = ("admin", "admin");

# Set the server domain and tool placement URL for T&Q

my $host = "http://localhost:8080";
my $rest = "/direct/sms-account";

my $ua= LWP::UserAgent->new;

$ua->timeout(30);
$ua->env_proxy;
$ua->cookie_jar({ file => "$ENV{HOME}/.cookies.txt" });

# Login
print "Logging in as $username / $password\n";

die "Not authed\n" if !loginToSakai($ua, $host, $username, $password);

my $accountName = "myacct2";
my $siteid="9123-456-789q2";

my $acctid = createAccount($ua, $host, $accountName, $siteid);

if ($acctid ne "") {
    print "Account created: id = $acctid\n";
}
