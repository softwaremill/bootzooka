import bsBlur from './bsBlur.js';
import bsMatch from './bsMatch.js';
import focusOn from './focusOn.js';

export default ngModule => {
    bsBlur(ngModule);
    bsMatch(ngModule);
    focusOn(ngModule);
};
