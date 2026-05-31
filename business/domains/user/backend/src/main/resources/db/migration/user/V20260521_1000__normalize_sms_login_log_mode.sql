update login_log
set verification_mode = 'SMS_CODE'
where verification_mode = 'TEST_CODE';
