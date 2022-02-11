# pop3-client

[![CodeQL](https://github.com/GustasG/pop3-client/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/GustasG/pop3-client/actions/workflows/codeql-analysis.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/905135be9d334e90998a0ac9dfbf4959)](https://www.codacy.com/gh/GustasG/pop3-client/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=GustasG/pop3-client&amp;utm_campaign=Badge_Grade)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/GustasG/pop3-client/master/LICENSE)

POP3 client that was created for computer networks course.

This client was implemented according to [RFC1939](https://www.ietf.org/rfc/rfc1939.txt) specification


## Implemented commands

- USER
- PASS
- QUIT
- NOOP
- STAT
- LIST [msg]
- DELE
- RSET
- UIDL [msg]
- RETR

## Missing commands

- TOP
- APOP