function SmsPopup() {
}

SmsPopup.prototype = {
    constructor: SmsPopup,
    createPopup: function() {
      if (!this.popup) {
        var self = this;
        //Create sms popup elements, append as necessary
        var popup = this.generateElement('div', {id: 'smsPopup'});
        var heading = this.generateElement('h4', {text: 'Choose Recipients'});
        var closeButton = this.generateElement('div', {class: 'closeButton', title: 'Close Popup'});
        var popupContent = this.generateElement('div', {id: 'smsContent'});
      
        popup.appendChild(heading);
        popup.appendChild(closeButton);
        popup.appendChild(popupContent);

      //Add event listeners
        closeButton.addEventListener('click', self.closePopup, false);
        this.popup = popup;
      }
      return this.popup;
    },
    generateElement: function(elType, params) {
      if (typeof elType != 'string') {
        return;
      }
      var el = document.createElement(elType);
      if (!params) {
        return el;
      }
      for (var key in params) {
        if (key == 'text') {
          el.innerHTML = params[key];
        }
        else {
          el.setAttribute(key, params[key]);
        }
      }
      return el;
    },
    displayPopup: function(params, callback) {
      this.popup.classList.add('active');
      if (params) {
        if (params.url && this.popup.getAttribute('data-loaded') != params.url) {
          var selector = params.selector || null;
          var cb = callback || null;
          this.fetchPage(params.url, selector, cb);
        }
        else if (params.url) {
          this.showPopupToUser();
        }
      }
    },
    closePopup: function(e) {
      document.querySelector('#smsPopup')
        .classList.remove('active');
    },
    switchView: function(e) {
      e.preventDefault();
      if (this.classList.contains('disabled') || this.classList.contains('active')) {
        return;
      }
      var popup = document.querySelector('#smsPopup');
      //Hide current view
      var curView = popup.querySelector('#peopleList div.active');
      var tabActive = popup.querySelector('#peopleList ul a.active');
      if (curView) {
        curView.classList.remove('active');
      }
      if (tabActive) {
        tabActive.classList.remove('active');
      }
      //Find and display new view
      var newId = (this.href.split('#'))[1];
      var newView = popup.querySelector('#' + newId);
      if (newView) {
        newView.classList.add('active');
      }
      this.classList.add('active');
    },
    showPopupToUser: function() {
      if (!this.popup.classList.contains('active')) {
        this.popup.classList.add('active');
      }
    },
    fetchPage: function(url, selector, cb) {
      var self = this;
      var popupContent = self.popup.querySelector('#smsContent');
      this.showPopupToUser();
      this.load({url: url, responseType: 'document'}, function(xhr) {
        self.popup.setAttribute('data-loaded', url);
        var doc = xhr.response;
        var content = (selector ? doc.querySelector(selector) : doc);
        if (!content) {
          if (selector) {
            popupContent.innerHTML = 'Selector not found in retrieved document';
          }
        }
        else {
          popupContent.appendChild(content);
          if (cb && typeof cb == 'function') {
            cb();
          }
        }
      }, function (err) {
        popupContent.innerHTML = 'Could not fetch page';
      });
    },
    load: function(params, cb, fail) {
      if (!params.url) {
        if (fail && typeof fail == 'function')
          fail(new Error('No URL provided'));
        else return;
      }
      var xhr = new XMLHttpRequest();
      xhr.open('GET', params.url, true);
      if (params.responseType) {
        xhr.responseType = params.responseType;
      }
      xhr.onload = function() {
        if (xhr.status < 400) {
          if (cb && typeof cb == 'function') {
            cb(xhr);
          }
        }
        else if (fail && typeof fail == 'function') {
          fail(xhr);
        }
      }
      xhr.send();
    },
    getElements: function(selector) {
      if (!selector) return;
      return this.popup.querySelectorAll(selector);
    },
    fetchSiteUsers: function(siteId, cb) {
      var url = '/direct/sms-task/memberships/site/' + siteId + '.json?_=' + (new Date().getTime()/1000);
      this.load({url: url}, function(xhr) {
        try {
          var json = JSON.parse(xhr.response);
          if (cb) {
           cb(jsonUsers.sms-task_collection);
          }
        } catch(e) {
          if (cb) {
            cb([]);
          }
        }
      });
    }
  }
