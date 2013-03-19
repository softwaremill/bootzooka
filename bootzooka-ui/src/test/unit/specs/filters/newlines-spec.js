'use strict';

describe("Newlines filter", function () {

    beforeEach(module('smlBootzooka.filters'));

    it('should transform newlines in paragraph to html line breaks', inject(function(newlinesFilter) {
            expect(newlinesFilter('line1\nline2')).toEqual('line1<br/>line2');
        }));
});