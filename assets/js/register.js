var SERVER = 'http://fzsmsonline.appspot.com';

function setAccounts(accounts){
	Accounts = accounts.split(',');
	$.mobile.changePage($('#register'), { transition: "none"});
	for(var i=0; i<Accounts.length; i++){
		$('#account-list').append("<li><a href='#'>"+Accounts[i]+"</a></li>");
	}
	$("#account-list").listview();
}
var registered = false;
function register_device_token(token){
	if(registered){
		$.mobile.loading( 'hide');
 	   $.mobile.changePage($('#stats-page'), { transition: "none"});
	}
	if (Get_Cookie("email")){
		$.ajax({
	           url: SERVER+ '/push_register/?callback=?',
	           beforeSend:function(a,b){Device.log(b.url)},
	           data: {
	        	   email:Get_Cookie("email"),
	        	   apid:token
	           },
	           success: function(data) {
	        	   registered = true;
	        	   Device.log("success");
	        	   $.mobile.loading( 'hide');
	        	   $.mobile.changePage($('#stats-page'), { transition: "none"});
	           },
	           error: function (x, t, m){
	        	   Device.log("error")
	           },
	           dataType: 'jsonp'
	       });
	} else {
		setTimeout(function(){register_device_token(token)}, 500);
	}
}
$(function(){
	//Delete_Cookie("email");
	Device.getAccounts();
	if(Get_Cookie("email")){
		$.mobile.changePage($('#stats-page'), { transition: "none"});
	}
	$('#account-list li a').live('touchend', function(e){
		e.preventDefault();
		//$.mobile.loading( 'show');
		var email = $(this).html();
		Set_Cookie("email", email);
		Device.saveEmail(email);
		$.mobile.changePage($('#stats-page'), { transition: "none"});
		return false;
	});
	$('#register-form').bind('submit', function(e){
		e.preventDefault();
		Device.log("Register");
		
		var phonenum = $('#phonenumber').val();
		var password = $('#password').val();
		Device.log(phonenum)
		Device.log(SERVER+ '/register/?callback=?')
		$.ajax({
	           url: SERVER+ '/register/?callback=?',
	           beforeSend:function(a,b){Device.log(b.url)},
	           data: {
	        	   phone:phonenum,
	        	   password:password
	           },
	           success: function(data) {
	        	   Device.log("success"+data.sid)
	               Set_Cookie("sid", data.sid);
	               $.mobile.changePage($('#stats-page'), { transition: "none"});
	               Device.syncContact();
	           },
	           error: function (x, t, m){
	        	   Device.log("error")
	           },
	           dataType: 'jsonp'
	       });
		//Device.syncContact();
	})
})
