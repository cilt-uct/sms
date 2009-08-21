#! /usr/bin/perl

## Update SMS accounts based on assigned issues in JIRA.
## 1. Get a list of JIRA issues via RSS
## 2. For each assigned open issue,
##     a. Get the siteId and number of credits, optionally account notification address
##     b. If necessary, create account, then credit the account associated with the site
##     c. If OK, email the account notification address CC help.
##     d. If not OK, assign the JIRA issue back to Help and email.

require LWP::UserAgent;
require HTTP::Cookies;
require HTTP::Request;
use HTTP::Request::Common qw(PUT POST);

require 'smslib.pl';
require 'sakairest.pl';

use strict;

# Include the username/password details - requires an admin-equivalent account

#require '/usr/local/sakaiconfig/loadtestauth.pl';
#(my $username, my $password) = getSakaiAuth();

(my $username, my $password) = ("admin", "admin");

# Set the server domain and tool placement URL for T&Q

my $host = "http://localhost:8080";

my $ua= LWP::UserAgent->new;
$ua->timeout(10);
$ua->env_proxy;
$ua->cookie_jar({ file => "$ENV{HOME}/.cookies.txt" });

# Login to Sakai
die "Not authed\n" if !loginToSakai($ua, $host, $username, $password);

# Login to JIRA

my @jiras = ("CETSMS-1", "CETSMS-2");

for my $jira (@jiras) {
    print "handling issue: $jira\n";
    
    my $siteId = "!admin";
    my $ownerEid = "scm";
    my $credits = 500;
    my $eid = "scm";
    
    # Site exists?
    
    my $siteName = getSiteName($ua, $host, $siteId);
    if ($siteName eq "") {
        reassign_failed_issue($jira, "Site ID $siteId does not exist");
        next;      
    }
    
    # User exists?
    my $ownerEmail = getUserEmail($ua, $host, $eid);
    if ($ownerEmail eq "") {
        reassign_failed_issue($jira, "User $eid does not exist or does not have an email address");
        next;      
    }
    
    # Check account for this site
    my $accountId = getAccountIdForSite($ua, $host, $siteId);
    
    # Create if necessary
    if ($accountId eq "") {
        $accountId = createAccount($ua, $host, $siteName, $siteId);
    }
    
    if ($accountId eq "") {
        # Creation failed
        reassign_failed_issue($jira, "Account could not be created");
        next;
    } 
    
    # Assign credits
    my $newbalance = creditAccount($ua, $host, $accountId, $credits, $jira);
    
    if ($newbalance eq "") {
        reassign_failed_issue($jira, "Account $accountId could not be credited");
        next;  
    }
    
    # Success - send email
    notify_owner($newbalance);
    resolve_issue();
}

sub notify_owner($)
{
    my $balance = shift;
    
    print "Done!\n";
    print "Closing balance: $balance\n";
}

sub reassign_failed_issue($$) 
{
  my $jira = shift;
  my $reason = shift;
 
  print "Reassigning $jira for reason: $reason\n"; 
}

sub resolve_issue($$)
{
  my $jira = shift;
  my $comment = shift;

  print "Resolving issue $jira with detail: $comment\n"; 

}