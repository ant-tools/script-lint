$package('comp.prj');

/**
 * Class.
 * @constructor
 * Constructor.
 */
comp.prj.Class = function() {
    return 123;
};

/**
 * Static method.
 * @return Number
 */
comp.prj.Class.staticMethod = function() {
    return 'value';
};

comp.prj.Class.prototype =
{
    /**
     * Method.
     * @return Number
     */
    method: function() {
        return true;
    }
};
$extends(comp.prj.Class, js.lang.Object);

/**
 * Class.
 * @constructor
 * Constructor.
 */
comp.prj.Class.InnerClass = function() {
    return 123;
};

/**
 * Static method.
 * @return Number
 */
comp.prj.Class.InnerClass.staticMethod = function() {
    return;
};

comp.prj.Class.InnerClass.prototype =
{
    /**
     * Method.
     * @return Number
     */
    method: function() {
        return {};
    }
};
$extends(comp.prj.Class.InnerClass, js.lang.Object);

$legacy(js.ua.Engine.TRIDENT, function() {
    /**
     * @return String
     */
    comp.prj.Class.prototype.method = function() {
        return 123;
    }
});
