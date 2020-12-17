$strict();
$package('comp.prj');

/**
 * Class.
 * @constructor
 * Constructor.
 */
comp.prj.Class = function() {
    /**
     * Field.
     * @type Number
     */
    this._field = null;

    /**
     * Constant.
     * @type Number
     */
    this._CONSTANT = 123;
};

/**
 * Static field.
 * @type Number
 */
comp.prj.Class._staticField = null;

/**
 * Static constant.
 * @type Number
 */
comp.prj.Class._STATIC_CONSTANT = 123;

/**
 * Static method.
 */
comp.prj.Class.staticMethod = function() {
    comp.prj.Class._staticField = 123;
};

/**
 * Class static initializer.
 */
$static(comp.prj.Class, function() {

});

comp.prj.Class.prototype =
{
    /**
     * Method.
     */
    method: function() {
        this._field = 123;
        comp.prj.Class._staticField = 123;
        var field = this._field;
        var staticField = comp.prj.Class._staticField;
        var CONSTANT = comp.prj.Class._STATIC_CONSTANT;
    }
};
$extends(comp.prj.Class, js.lang.Object);

/**
 * Class.
 * @constructor
 * Constructor.
 */
comp.prj.Class.InnerClass = function() {
    /**
     * Field.
     * @type Number
     */
    this._innerField = null;

    /**
     * Constant.
     * @type Number
     */
    this._INNER_CONSTANT = 123;
};

/**
 * Static field.
 * @type Number
 */
comp.prj.Class.InnerClass._innerStaticField = null;

/**
 * Static constant.
 * @type Number
 */
comp.prj.Class.InnerClass._INNER_STATIC_CONSTANT = 123;

/**
 * Static method.
 */
comp.prj.Class.InnerClass.staticMethod = function() {
};

comp.prj.Class.InnerClass.prototype =
{
    /**
     * Method.
     */
    method: function() {
        this._innerField = 123;
        comp.prj.Class.InnerClass._innerStaticField = 123;
    }
};
$extends(comp.prj.Class.InnerClass, js.lang.Object);

/**
 * Legacy code.
 */
$legacy(js.ua.Engine.TRIDENT, function() {
    comp.prj.Class.prototype.method = function() {
        this._field = 123;
        comp.prj.Class._staticField = 123;
    };

    comp.prj.Class.InnerClass.prototype.method = function() {
        this._innerField = 123;
        comp.prj.Class.InnerClass._innerStaticField = 123;
    };
});
