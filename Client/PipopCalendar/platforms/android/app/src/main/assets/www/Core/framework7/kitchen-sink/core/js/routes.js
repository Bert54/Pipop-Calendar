var mainView = app.views.create('.view-main', {
  stackPages: true,
  routes: [
    {
      path: '/panel-left/',
      panel: {
        componentUrl: './pages/panels/left.html'
      }
    },
    {
      path: '/panel-right/',
      panel: {
        componentUrl: './pages/panels/right.html'
      }
    },
    {
      path: '/user-home/',
      componentUrl: './user-home.html',
      name: 'user-home'
    },
    {
      path: '/themes/',
      componentUrl: './pages/themes.html',
      name: 'themes'
    },
		{
      path: '/personnal-info/',
      componentUrl: './pages/personnal-info.html',
      name: 'personnal-info'
    },
    {
      path: '/preloaderTheme/',
      componentUrl: './pages/preloaderTheme.html',
      name: 'preloaderTheme'
    },
    {
      path: '/calendar-form/',
      componentUrl: './pages/calendar-form.html',
      name: 'calendar-form',
      on: {
          pageAfterIn: function (e, page) {
            console.log("Chargement formulaire");
            $.ajax({
              url: "js/creerCalendrier.js",
              dataType: "script",
              cache: true,
              success:function(msg) {
              },
              error:function(msg) {
                console.log("Error chargement script de création de calendrier");
              }
            });
          },
          pageAfterOut: function(e,page){
              $.ajax({
                url: "js/chargerCalendrier.js",
                dataType: "script",
                cache: true,
                success:function(msg) {
                },
                error:function(msg) {
                  console.log("Error chargement script de chargement de calendrier");
                }
              });
          }
      }
    },
    {
      path: '/calendar-form-edit/',
      componentUrl: './pages/calendar-form-edit.html',
      name: 'calendar-form-edit',
      on: {
          pageAfterOut: function(e,page){
              $.ajax({
                url: "js/modifierCalendrier.js",
                dataType: "script",
                cache: true,
                success:function(msg) {
                },
                error:function(msg) {
                  console.log("Error chargement script de chargement de calendrier");
                }
              });
          }
      }
    },
    {
      path: '/calendar-view/',
      componentUrl: './pages/calendar-view.html',
      name: 'calendar-view',
      on:{
        pageAfterIn: function (e, page) {
           $.ajax({
             url: "js/supprimerCalendrier.js",
             dataType: "script",
             cache: true,
             success:function(msg) {
             },
             error:function(msg) {
               console.log("Error chargement script de suppression de calendrier");
             },
           });
           $.ajax({
             url: "js/modifierCalendrier.js",
             dataType: "script",
             cache: true,
             success:function(msg) {
             },
             error:function(msg) {
               console.log("Error chargement script de modification de calendrier");
             },
           });
         }
      }
    },
    {
     path: '/event-form/',
     componentUrl: './pages/event-form.html',
     name: 'event-form',
     on:{
       pageAfterIn: function (e, page) {
         $.ajax({
           url: "js/creerEvenement.js",
           dataType: "script",
           cache: true,
           success:function(msg) {
           },
           error:function(msg) {
             console.log("Error chargement script de création d'événement");
           },
         })
       }
     }
   },
    // Default route, match to all pages (e.g. 404 page)
    {
      path: '(.*)',
      url: './pages/404.html'
    }
  ]
});
