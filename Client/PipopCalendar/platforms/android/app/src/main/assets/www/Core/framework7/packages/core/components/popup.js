(function framework7ComponentLoader(o,p){void 0===p&&(p=!0);document,window;var a=o.$,e=(o.Template7,o.utils),r=(o.device,o.support,o.Class,o.Modal),t=(o.ConstructorMethods,o.ModalMethods),c=function(o){function p(p,r){var t=e.extend({on:{}},p.params.popup,r);o.call(this,p,t);var c,n,l=this;if(l.params=t,(c=l.params.el?a(l.params.el):a(l.params.content))&&c.length>0&&c[0].f7Modal)return c[0].f7Modal;if(0===c.length)return l.destroy();function s(o){var p=o.target;if(0===a(p).closest(l.el).length&&l.params&&l.params.closeByBackdropClick&&l.params.backdrop&&l.backdropEl&&l.backdropEl===p){var e=!0;l.$el.nextAll(".popup.modal-in").each(function(o,p){var a=p.f7Modal;a&&a.params.closeByBackdropClick&&a.params.backdrop&&a.backdropEl===l.backdropEl&&(e=!1)}),e&&l.close()}}return l.params.backdrop&&0===(n=p.root.children(".popup-backdrop")).length&&(n=a('<div class="popup-backdrop"></div>'),p.root.append(n)),e.extend(l,{app:p,$el:c,el:c[0],$backdropEl:n,backdropEl:n&&n[0],type:"popup"}),l.on("popupOpened",function(){l.params.closeByBackdropClick&&p.on("click",s)}),l.on("popupClose",function(){l.params.closeByBackdropClick&&p.off("click",s)}),c[0].f7Modal=l,l}return o&&(p.__proto__=o),p.prototype=Object.create(o&&o.prototype),p.prototype.constructor=p,p}(r),n={name:"popup",params:{popup:{backdrop:!0,closeByBackdropClick:!0}},static:{Popup:c},create:function(){this.popup=t({app:this,constructor:c,defaultSelector:".popup.modal-in"})},clicks:{".popup-open":function(o,p){void 0===p&&(p={});this.popup.open(p.popup,p.animate)},".popup-close":function(o,p){void 0===p&&(p={});this.popup.close(p.popup,p.animate)}}};if(p){if(o.prototype.modules&&o.prototype.modules[n.name])return;o.use(n),o.instance&&(o.instance.useModuleParams(n,o.instance.params),o.instance.useModule(n))}return n}(Framework7, typeof Framework7AutoInstallComponent === 'undefined' ? undefined : Framework7AutoInstallComponent))