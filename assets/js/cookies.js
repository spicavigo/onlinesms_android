OS="Android";
function Get_Cookie( check_name ) {
	if (OS == 'iPad' || OS == 'iPhone') {
        return CookieJar[check_name];
    }
	else {
		return window.localStorage.getItem(check_name);
	}
    // first we'll split this cookie up into name/value pairs
    // note: document.cookie only returns name=value, not the other components
    var a_all_cookies = document.cookie.split( ';' );
    var a_temp_cookie = '';
    var cookie_name = '';
    var cookie_value = '';
    var b_cookie_found = false; // set boolean t/f default f
    var i = '';
    
    for ( i = 0; i < a_all_cookies.length; i++ )
    {
        // now we'll split apart each name=value pair
        a_temp_cookie = a_all_cookies[i].split( '=' );
        
        
        // and trim left/right whitespace while we're at it
        cookie_name = a_temp_cookie[0].replace(/^\s+|\s+$/g, '');
    
        // if the extracted name matches passed check_name
        if ( cookie_name == check_name )
        {
            b_cookie_found = true;
            // we need to handle case where cookie has no value but exists (no = sign, that is):
            if ( a_temp_cookie.length > 1 )
            {
                cookie_value = unescape( a_temp_cookie[1].replace(/^\s+|\s+$/g, '') );
            }
            // note that in cases where cookie is initialized but no value, null is returned
            return cookie_value;
            break;
        }
        a_temp_cookie = null;
        cookie_name = '';
    }
    if ( !b_cookie_found ) 
    {
        return null;
    }
}

/*
 only the first 2 parameters are required, the cookie name, the cookie
 value. Cookie time is in milliseconds, so the below expires will make the 
 number you pass in the Set_Cookie function call the number of days the cookie
 lasts, if you want it to be hours or minutes, just get rid of 24 and 60.
 
 Generally you don't need to worry about domain, path or secure for most applications
 so unless you need that, leave those parameters blank in the function call.
 */
function Set_Cookie( name, value, expires, path, domain, secure ) {
	if (OS == 'iPad' || OS == 'iPhone'){
		window.localStorage.removeItem(name);
    	window.localStorage.setItem(name, value);
    	CookieJar[name] = value;
		return;
	}    
	else {  
		window.localStorage.removeItem(name);
    	window.localStorage.setItem(name, value);  
        
	}

    // set time, it's in milliseconds
    var today = new Date();
    today.setTime( today.getTime() );
    // if the expires variable is set, make the correct expires time, the
    // current script below will set it for x number of days, to make it
    // for hours, delete * 24, for minutes, delete * 60 * 24
    if ( expires )
    {
        expires = expires * 1000 * 60 * 60 * 24;
    }
    //alert( 'today ' + today.toGMTString() );// this is for testing purpose only
    var expires_date = new Date( today.getTime() + (expires) );
    //alert('expires ' + expires_date.toGMTString());// this is for testing purposes only
    
    document.cookie = name + "=" +escape( value ) +
        ( ( expires ) ? ";expires=" + expires_date.toGMTString() : "" ) + //expires.toGMTString()
        ( ( path ) ? ";path=" + path : "" ) + 
        ( ( domain ) ? ";domain=" + domain : "" ) +
        ( ( secure ) ? ";secure" : "" );
    
}

// this deletes the cookie when called
function Delete_Cookie( name, path, domain ) {
	if (OS == 'iPad' || OS == 'iPhone'){
    	window.localStorage.removeItem(name);
        delete CookieJar[name];
    	return;
	} else {
		window.localStorage.removeItem(name);
    	return;
	}
    if ( Get_Cookie( name ) ) document.cookie = name + "=" +
            ( ( path ) ? ";path=" + path : "") +
            ( ( domain ) ? ";domain=" + domain : "" ) +
            ";expires=Thu, 01-Jan-1970 00:00:01 GMT";
}

function objectToString(o){
    
    var parse = function(_o){
    
        var a = [], t;
        
        for(var p in _o){
        
            if(_o.hasOwnProperty(p)){
            
                t = _o[p];
                
                if(t && typeof t == "object"){
                
                    a[a.length]= p + ":{ " + arguments.callee(t).join(", ") + "}";
                    
                }
                else {
                    
                    if(typeof t == "string"){
                    
                        a[a.length] = [ p+ ": \"" + t.toString() + "\"" ];
                    }
                    else{
                        a[a.length] = [ p+ ": " + t.toString()];
                    }
                    
                }
            }
        }
        
        return a;
        
    }
    
    return "{" + parse(o).join(", ") + "}";
    
}