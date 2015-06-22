'use strict';

describe('Date & Time filters:', function () {

    beforeEach(module('smlBootzooka.common.filters'));

    describe('relative date filter', function () {
        it('should convert JS date object to relative date string', inject(function (relativeDateFilter) {
            expect(relativeDateFilter(moment().subtract(3, 'hours').toDate())).toBe('3 hours ago');
            expect(relativeDateFilter(moment().subtract(1, 'day').toDate())).toBe('a day ago');
            expect(relativeDateFilter(new Date())).toBe('a few seconds ago');
        }));
    });

    describe('date only filter', function () {
        it('should convert JS date object to relative date string', inject(function (utcDateOnlyFilter) {
            expect(utcDateOnlyFilter(moment('2015-01-19T12:35:03Z').toDate())).toBe('January 19th, 2015');
            expect(utcDateOnlyFilter(moment('2015-01-01TZ').toDate())).toBe('January 1st, 2015');
            expect(utcDateOnlyFilter(moment('2014-12-03TZ').toDate())).toBe('December 3rd, 2014');
        }));
    })

});
