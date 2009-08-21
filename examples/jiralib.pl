#! /usr/bin/perl

## JIRA Library Functions - c/f http://confluence.atlassian.com/display/JIRA/Creating+a+SOAP+Client

use JIRA::Client;
use SOAP::Lite;
use Data::Dumper;

## Get a comma-separated list of issues in the filter.

sub getIssuesForFilter($$$$) {
    my $host = shift;
    my $username = shift;
    my $password = shift;
    my $filter = shift;
    
    my $jira = JIRA::Client->new($host, $username, $password);

    my $issuelist = $jira->getIssuesFromFilter($filter);    

    my @issues = ();
    for my $issue (@{$issuelist}) {
        push(@issues, $issue->{key});
    }
    
    return @issues;
}

sub getIssueDetails($$$$) {
    my $host = shift;
    my $username = shift;
    my $password = shift;
    my $issuekey = shift;

    ## These custom field values are specific to a JIRA installation
    
    my $custom_username = "customfield_10000";
    my $custom_siteid = "customfield_10001";
    my $custom_credits = "customfield_10002";
    
    my $jira = JIRA::Client->new($host, $username, $password);
    
    $issue = $jira->getIssue($issuekey);
    
    my $siteid = "";
    my $username = "";
    my $credits = "";
    
    for $customfield (@{$issue->{customFieldValues}}) {
    
        if ($customfield->{customfieldId} eq $custom_siteid) {
            $siteid = $customfield->{values}[0];
        }
    
        if ($customfield->{customfieldId} eq $custom_username) {
            $username = $customfield->{values}[0];
        }
        
        if ($customfield->{customfieldId} eq $custom_credits) {
            $credits = $customfield->{values}[0];
        }
    }
    
    return ($siteid, $username, $credits);
}

sub assignIssue($$$$$$) {

    my $host = shift;
    my $username = shift;
    my $password = shift;
    my $issuekey = shift;
    my $assignee = shift;
    my $comment = shift;
    
    my $jira = JIRA::Client->new($host, $username, $password);
    
    $jira->updateIssue($issuekey,
       {
         assignee => $assignee,
       }
     );

    $jira->addComment($issuekey, $comment);
 
}

sub resolveIssue($$$$$) {

    my $host = shift;
    my $username = shift;
    my $password = shift;
    my $issuekey = shift;
    my $comment = shift;
    
    my $jira = JIRA::Client->new($host, $username, $password);
        
    $jira->progressWorkflowAction($issuekey, 5,
       {
         resolution => 1,
       }
     );

   $jira->addComment($issuekey, $comment);
}

return 1;

############

$VAR1 = bless( {
                 'priority' => '3',
                 'customFieldValues' => [
                                        bless( {
                                                 'customfieldId' => 'customfield_10001',
                                                 'values' => [
                                                             '!admin'
                                                           ],
                                                 'key' => undef
                                               }, 'RemoteCustomFieldValue' ),
                                        bless( {
                                                 'customfieldId' => 'customfield_10000',
                                                 'values' => [
                                                             '01404877'
                                                           ],
                                                 'key' => undef
                                               }, 'RemoteCustomFieldValue' )
                                      ],
                 'status' => '1',
                 'project' => 'CETSMS',
                 'components' => [],
                 'attachmentNames' => [],
                 'reporter' => 'smarquard',
                 'key' => 'CETSMS-6',
                 'assignee' => 'vulahelp',
                 'summary' => 'Test custom fields',
                 'updated' => '2009-08-21T18:09:02.000Z',
                 'id' => '10867',
                 'votes' => '0',
                 'fixVersions' => [],
                 'affectsVersions' => [],
                 'description' => 'testing custom stuff description',
                 'environment' => undef,
                 'created' => '2009-08-21T18:09:02.000Z',
                 'resolution' => undef,
                 'type' => '3',
                 'duedate' => undef
               }, 'RemoteIssue' );

#########
