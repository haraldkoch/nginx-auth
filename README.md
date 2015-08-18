# auth

nginx-auth is a simple form-based authentication server designed to be used with
the auth_request plugin of nginx. It allows you to use form-based authentication
instead of basic-auth for authenticating your users.

## Deployment Example

    location /private/ {
        auth_request /auth-check;
        # redirect 401 and 403 to login form
        error_page 401 403 =200 /nginx-auth/;
    }

    location /nginx-auth/ {
        proxy_pass http://localhost:3000/;
        proxy_redirect http://localhost:3000/ /;
        # Login service returns a redirect to the original URI
        # and sets the cookie for auth-check
        proxy_set_header X-Target $request_uri;
    }

    location = /auth-check {
        internal;
        proxy_pass http://localhost:3000/auth-check;
        proxy_pass_request_body off;
        proxy_set_header Content-Length "";
        proxy_set_header X-Original-URI $request_uri;
    }

## Prerequisites

An [nginx][1] server with the [auth_request][2] module installed;
this is the default on most modern Debian/Ubuntu/Arch systems.

[1]: http://nginx.org/
[2]: http://nginx.org/en/docs/http/ngx_http_auth_request_module.html

## Running

You need to set the environment variable APP_CONTEXT (or :app-context) to match the proxy location above.

To start a web server for the application, run:

    lein run

## License

Copyright © 2015 C. Harald Koch.
