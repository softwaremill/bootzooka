angular.module('smlBootzooka.common.directives')

/**
 * Makes element with this directive visible when there are http requests pending
 * and hides it with fadeOut when all requests are completed.
 * If you need to track only certain requests (e.g. only those for loading commits list) provide desired requestType as value of 'http-request-tracker' attribute.
 * Request type provided must match one specified on $http call e.g. $http({method:..., requestType: 'list'}) should be used with http-request-tracker='list'
 */
    .directive('bsHttpRequestTracker', function($http) {
        return {
            restricted: 'A',
            scope: {},
            link: function(scope, element, attrs) {
                var requestType = attrs.httpRequestTracker;
                var activeClass = attrs.activeClass || '';

                function observedRequestsCount() {
                    var pendingRequests = $http.pendingRequests;
                    if (requestType) {
                        return pendingRequests.filter(function(request) {
                            return request.requestType === requestType;
                        }).length;
                    } else {
                        return pendingRequests.length;
                    }
                }

                scope.$watch(function() {
                    return $http.pendingRequests.length;
                }, function() {
                    if(observedRequestsCount()) {
                        element.show();
                        element.addClass(activeClass);
                        return;
                    }
                    element.fadeOut();
                    element.removeClass(activeClass);
                });
            }
        };
    });