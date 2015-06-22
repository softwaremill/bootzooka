'use strict';

describe('Truncate filter', function () {

    var loremIpsum = 'Lorem ipsum dolor sit amet';

    beforeEach(module('smlBootzooka.common.filters'));

    it('should truncate string', inject(function (truncateFilter) {
        expect(truncateFilter(loremIpsum)).toBe('Lorem i...');
        expect(truncateFilter(loremIpsum, 8)).toBe('Lorem...');
        expect(truncateFilter(loremIpsum, 7, '@@')).toBe('Lorem@@');
    }));

});
