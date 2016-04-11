export default ngModule => {
  /**
   * Makes element with this directive visible when there are http requests pending
   * and hides it with fadeOut when all requests are completed.
   * If you need to track only certain requests (e.g. only those for loading commits list) provide desired requestType as value of 'http-request-tracker' attribute.
   * Request type provided must match one specified on $http call e.g. $http({method:..., requestType: 'list'}) should be used with http-request-tracker='list'
   */
  ngModule.directive('bsHttpRequestTracker', ($http) => {
    return {
      restricted: 'A',
      transclude: true,
      scope: {},
      link: (scope, element, attrs) => {
        let requestType = attrs.httpRequestTracker;
        let activeClass = attrs.activeClass || '';

        function observedRequestsCount() {
          var pendingRequests = $http.pendingRequests;
          if (requestType) {
            return pendingRequests.filter(request => {
              return request.requestType === requestType;
            }).length;
          } else {
            return pendingRequests.length;
          }
        }

        scope.$watch(() => {
          return $http.pendingRequests.length;
        }, () => {
          if (observedRequestsCount()) {
            element.show();
            element.addClass(activeClass);
            return;
          }
          element.fadeOut();
          element.removeClass(activeClass);
        });
      }
    };
  })
}
