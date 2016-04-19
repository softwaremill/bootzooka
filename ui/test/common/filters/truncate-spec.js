'use strict';

describe('Truncate filter', function () {

    var loremIpsum = 'Lorem ipsum dolor sit amet';

    beforeEach(angular.mock.module('smlBootzooka.common'));

    it('should truncate string', angular.mock.inject(function (truncateFilter) {
        expect(truncateFilter(loremIpsum)).toBe('Lorem i...');
        expect(truncateFilter(loremIpsum, 8)).toBe('Lorem...');
        expect(truncateFilter(loremIpsum, 7, '@@')).toBe('Lorem@@');
    }));

});
