var app = new Framework7({
  // App root element
  root: '#app',
  // App Name
  name: 'Pipop Calendar'
});

var $$ = Dom7;

var $ptrContent = $$('.ptr-content');
$ptrContent.on('ptr:refresh', function (e) {
  // Emulate 2s loading
  setTimeout(function () {
    chargerCalendriers(localStorage.getItem("emailUtilisateur"));
    app.ptr.done(); // or e.detail();
  }, 2000);
});

document.addEventListener("backbutton", onBackKeyDown, false);

function onBackKeyDown() {
  var path = window.location.pathname;
  var page = path.split("/").pop();
  if(page != "index.html"){
    app.dialog.confirm('Are you sure you want to log out?', function () {
        localStorage.setItem("emailUtilisateur","");
        window.location = "index.html";
    });
  }else{
    app.dialog.confirm('Do you really want to exit Pipop?', function () {
            localStorage.setItem("emailUtilisateur","");
            window.navigator.app.exitApp();
    });
  }
}

var adresse = 'http://tomgalanx.ovh:3307';//'http://f05ca36f.ngrok.io/';//http://tomgalanx.ovh:3307';//'http://10.0.2.2:3307';//'http://a85b6f81.ngrok.io';
