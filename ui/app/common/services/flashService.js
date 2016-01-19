'use strict';

angular.module('smlBootzooka.common.services').factory('FlashService', () => {

  let queue = [];

  return {
    set: message => queue.push(message),

    get: () => queue.shift()
  };
});
