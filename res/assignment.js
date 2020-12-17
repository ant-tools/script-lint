$strict();
$package('comp.prj');

/**
 * @constructor
 */
comp.prj.Class = function() {

}

comp.prj.Class.prototype =
{
    /**
     * Returns a string representation of the object.
     *
     * @return String object string representation.
     */
    toString: function() {
        return 'js.net.XHR';
    }
};
$extends(comp.prj.Class, js.lang.Object);
