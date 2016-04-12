export default ngModule => {
  ngModule.factory('FlashService', () => {

    let queue = [];

    return {
      set: message => queue.push(message),

      get: () => queue.shift()
    };
  })
}
