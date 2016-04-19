export default ngModule => {
  ngModule.directive('bsNotificationEntry', $timeout => {

    function setUpElementAttributes(element, typeOrUndefined) {
      var type = typeOrUndefined || 'info';
      element.addClass('alert');
      element.addClass('alert-dismissible');
      element.addClass('alert-' + type);
      element.attr('role', 'alert');
    }

    return {
      restrict: 'A',
      template: require('./notificationEntry.html'),
      link: function (scope, element) {
        let alertFadeOutPromise = $timeout(() => {
          element.fadeOut(2000, () => element.remove());
        }, 3000);

        setUpElementAttributes(element, scope.message.type);

        element.fadeOut(0, () => element.fadeIn(300));

        // enforce scope destroy after message is closed by the user.
        element.on('$destroy', () => scope.$destroy());

        scope.$on('$destroy', () => {
          $timeout.cancel(alertFadeOutPromise);
          scope.notificationsSource.dismiss(scope.message);
        });
      }
    };
  });
}
