$strict();
$suppress('NO_APIDOC');
$suppress('BAD_THIS');
$suppress('NO_FIELD_DECL_');
$suppress('NO_CLASS_DEF');
$suppress('BAD_BODY_DEF');
$suppress('BAD_RETURN');
$suppress('CONST_ASSIGN_');
$package('comp.prj');

// constructor definition
comp.prj.Class = function() {
    // instance field declaration from constructor
    /** @type Number */
    this.field1 = null;

    // instance constant declaration from constructor
    /** @type Number */
    this.CONSTANT1 = 123;

    this.CONSTANT1 = 123;

    // invalid class body definition from constructor
    comp.prj.Class.prototype = {};

    this.field1 = comp.prj.Class.staticField1;
};

// invalid global field declaration
this.globalField = null;

// static field declaration from global scope
/** @type Number */
comp.prj.Class.staticField1 = null;

// invalid static field declaration with function invocation returned value
/** @type Number */
comp.prj.Class.staticField2 = call();

// invalid static field declaration with function invocation returned value
/** @type js.net.XHR */
comp.prj.Class.staticField2 = new js.net.XHR();

// static constant declaration from global scope
/** @type Number */
comp.prj.Class.CONSTANT1 = 123;

// static method declaration from global scope
comp.prj.Class.staticMethod1 = function() {
    // static field assignment from static method
    comp.prj.Class.staticField1 = null;

    // static constant assignment from static method
    comp.prj.Class.CONSTANT1 = 123;

    // invalid static field assignment using this pointer from static method
    this.staticField1 = 123;

    // invalid class body definition from static method
    comp.prj.Class.prototype = {};
};

// class body definition from global scope
comp.prj.Class.prototype =
{
    // instance method definition from class body
    method1: function() {
        // instance field assignment from instance method
        this.field1 = 123;

        // instance field declaration from instance method
        this.field2 = 123;

        // instance constant assignment from instance method
        this.CONSTANT1 = 123;

        // instance constant declaration from instance method
        this.CONSTANT2 = 123;

        comp.prj.Classes.staticField1 = 123;
        comp.prj.Classes.staticMethod1 = function() {
        };
    },

    method2: function() {
        comp.prj.Classes.prototype.method3 = function() {
            return function() {
                this.field = 123;
            };
        };
    }
};

comp.prj.Classes.prototype.method2 = function() {
};

$legacy(true, function() {
    // invalid instance field assignment in anonymous function
    this.staticField1 = 123;

    comp.prj.Classes.staticField1 = null;

    comp.prj.Classes.staticMethod1 = function() {
    };

    comp.prj.Classes.prototype.method1 = function() {
    };

    // valid class body definition from legacy scope
    comp.prj.Class.prototype =
    {
        method1: function() {
        },

        method2: function() {
        }
    };
});
