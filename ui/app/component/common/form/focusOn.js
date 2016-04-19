'use strict';
export default ngModule => {
    ngModule.directive('focusOn', () =>
        (scope, elem) =>  elem[0].focus()
    );   
}
