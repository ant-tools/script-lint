// missing package definition

/**
 * @constructor
 */
comp.prj.Class = function() {
};

comp.prj.Class.prototype =
{
    /**
     * @return String
     */
    toString: function() {
        return 'comp.prj.Class';
    }
};
$extends(comp.prj.Class, js.lang.Object);
