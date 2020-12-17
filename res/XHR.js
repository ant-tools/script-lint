$strict();
$package('js.net');

/**
 * XML HTTP Request. This class name is a little misleading: it actually encapsulates
 * both request and related server response. Takes care to prepare HTTP request
 * package with application data, serialized as string, send it to server and wait
 * for response package. Deserialize server data accordingly response content type.
 *
 * <p>This class is a wrapper for built-in {@link XMLHttpRequest} closely following
 * its usage pattern:
 * <ol>
 * <li>create instance
 * <li>add event listeners
 * <li>open connection
 * <li>set request headers
 * <li>send data
 * </ol>
 * Steps 2 and 4 are optionally but order is mandatory. This class tracks its internal
 * state and assert if trying to abuse it in any way. An usage sample follows:
 * <pre>
 * 	var xhr = new js.net.XHR();
 * 	xhr.on('progress', this._onProgress, this);
 * 	xhr.on('load', this._onLoad, this);
 * 	xhr.open('POST', 'form-processor.xsp');
 * 	xhr.setHeader('X-Application', 'j(s)-app');
 * 	xhr.send(form);
 * </pre>
 *
 * <a id="events-description" />
 * XHR transaction events, a subset of XMLHttpRequest Level 2. This implementation
 * doesn't consider <b>loadstart</b> and <b>abort</b> since they are triggered by
 * user code actions.
 *
 * <p><table>
 * 	<tr><td><b>progress</b>
 * 		<td>Triggered periodically if current XHR transaction sends a form. It has
 * 		only one argument: a progress object that is an extension of W3C Progress
 * 		Event interface.
 *	<tr><td><b>error</b>
 *		<td>Fired on server side and networking failures. Note that this event is
 *		not triggered when application code from server throws exception.
 *	<tr><td><b>timeout</b>
 *		<td>Fired if transaction is not completed in specified amount of time. Because
 *		form transfer duration can vary from seconds to minutes timeout mechanism
 *		is actually enabled only when send objects and XML documents. Also if user
 *		code set this XHR transaction timeout value to zero this event is not triggered.
 *		Timeout event has no argument.
 *	<tr><td><b>load</b>
 *		<td>Fired when request successfully completed. This event has only one argument,
 *		namely the object arrived from server.
 *	<tr><td><b>loadend</b>
 *		<td>Request aborted or completed, either in error, timeout or successfully.
 *		This event has no argument.
 * </table>
 *
 * <p>Self-destruction. This class releases used resources after loadend event was
 * fired. User code must be aware of this feature and don't try to access this XHR
 * response attributes outside load event handler. Also, this class is not reentrant.
 * Do not try to reuse XHR instances; create a new one for every transaction.
 *
 * @constructor
 * Construct HTTP request transaction.
 */
js.net.XHR = function() {
    $assert(this instanceof js.net.XHR, 'js.net.XHR#XHR', 'Invoked as function.');

    /**
     * Asynchronous request worker.
     * @type XMLHttpRequest
     * @private
     */
    this._request = new XMLHttpRequest();

	/**
	 * This request instance state machine.
	 * @type js.net.XHR.StateMachine
	 */
	this._state = js.net.XHR.StateMachine.CREATED;

    /**
     * Synchronous mode flag. All XHR transaction are asynchronous, i.e. {@link #send}
     * returns immediately and user code should use events to acquire server response.
     * So this flag is false by default. Anyway, there are marginal use cases where
     * a synchronous response is more appropriate.
     * @type Boolean
     * @private
     */
    this._synchronousMode = false;

	/**
	 * Request timeout in milliseconds.
	 * @type js.util.Timeout
	 * @private
	 */
	this._timeout = new js.util.Timeout(0);
	this._timeout.setCallback(this._onTimeout, this);

    /**
     * Asynchronous request events.
     * @type js.event.CustomEvents
     * @private
     */
    this._events = new js.event.CustomEvents();
    this._events.register('progress', 'error', 'timeout', 'load', 'loadend');
};

/**
 * Default synchronous mode timeout.
 * @type Number
 */
js.net.XHR.SYNC_TIMEOUT = 4000;

/**
 * HTTP request header extension for XHR version.
 * @type String
 */
js.net.XHR.VERSION_HEADER = 'X-JSLIB-Version';

/**
 * HTTP request header extension for XHR request type.
 * @type String
 */
js.net.XHR.REQUEST_TYPE_HEADER = 'X-JSXHR-Request-Type';

/**
 * Valid header name and value.
 * @type RegExp
 */
js.net.XHR.VALID_HEADER = /^[A-Z0-9\-\/\s,\.]+$/gi;

js.net.XHR.prototype =
{
    /**
     * Class logger.
     * @type js.lang.Log
     */
    _log: LogFactory.getLogger('js.net.XHR'),

    /**
     * Add event listener. Listener function should have next signature:
     * <pre>
     * 	void listener(Value... args)
     * </pre>
     * where <em>args</em> are specific for every event type.
     * See <a href="#events-description">events description</a>.
     *
     * @param String type event type,
     * @param Function listener event listener to register,
     * @param Object scope listener run-time scope.
     * @return js.net.XHR this object.
     */
    on: function(type, listener, scope) {
		$assert(this._state === js.net.XHR.StateMachine.CREATED, 'js.net.XHR#on', 'Illegal state.');
        if (type === 'progress') {
        	this._request.upload.addEventListener('progress', function(ev) {
                this._events.fire('progress', ev);
			}.bind(this));
        }
        this._events.addListener(type, listener, scope || window);
        return this;
    },

    /**
     * Set transaction timeout. A timeout value of zero disable transaction timeout
     * that can hang indefinitely. If timeout value is not positive this method does
     * nothing.
     *
     * @param Number timeout transaction timeout in milliseconds.
     * @return js.net.XHR this object.
     * @assert given argument is a positive {@link Number}.
     */
    setTimeout: function(timeout) {
        $assert(js.lang.Types.isNumber(timeout), 'js.net.XHR#setTimeout', 'Timeout is not a number.');
        $assert(timeout >= 0, 'js.net.XHR#setTimeout', 'Timeout is not strict positive.');
		this._timeout.set(timeout);
        return this;
    },

    /**
     * Set request header. This setter is usable only after request is {@link #open opened}
     * but before {@link #send}. Header name and value should be valid as described
     * by {@link js.net.XHR#VALID_HEADER} pattern. Assert request is in proper state
     * and arguments are valid. Anyway, if assert is disabled values are sent to
     * native request as they are and rise exception if invalid.
     *
     * @param String header header name,
     * @param String value header value.
     * @return js.net.XHR this object.
     * @throws InvalidStateError if request is not in proper state.
     * @throws SyntaxError if header or value is invalid.
     * @assert request is in proper state and arguments are valid.
     */
    setHeader: function(header, value) {
		/**
		 * @param String str
		 * @return Boolean
		 */
		function isValid(str) {
			js.net.XHR.VALID_HEADER.lastIndex = 0;
			return str && js.net.XHR.VALID_HEADER.test(str);
		}
		$assert(this._state === js.net.XHR.StateMachine.OPENED, 'js.net.XHR#setHeader', 'Illegal state.');
        $assert(isValid(header), 'js.net.XHR#setHeader', 'Header name is invalid.');
        $assert(isValid(value), 'js.net.XHR#setHeader', 'Header value is invalid.');
		return this._setHeader(header, value);
    },

	/**
	 * Request header setter.
	 *
     * @param String header header name,
     * @param String value header value.
     * @return js.net.XHR this object.
	 */
	_setHeader: function(header, value) {
		this._request.setRequestHeader(header, value);
        return this;
	},

	/**
	 * Set request type. Request type is a string identifying application level
	 * service like <em>RMI</em>, <em>FORM</em>, etc.
	 *
	 * @param String requestType request type.
     * @return js.net.XHR this object.
	 */
    setRequestType: function(requestType) {
        return this.setHeader(js.net.XHR.REQUEST_TYPE_HEADER, requestType);
    },

    /**
     * Get response header. Note that response header is valid only after request
     * is successfully complete. Returns null if requested header is not found.
     *
     * @param String header, header name to be retrieved.
     * @return String the value of requested header or null.
     */
    getHeader: function(header) {
		$assert(this._state === js.net.XHR.StateMachine.DONE, 'js.net.XHR#getHeader', 'Illegal state.');
        return this._request.getResponseHeader(header);
    },

    /**
     * Get response status. Note that response status is valid only after request
     * is successfully complete.
     *
     * @return Number response status code as integer value.
     */
    getStatus: function() {
		$assert(this._state === js.net.XHR.StateMachine.DONE, 'js.net.XHR#getStatus', 'Illegal state.');
        return window.parseInt(this._request.status, 10);
    },

    /**
     * Get response status text. Useful only for debug; application developer is
     * encouraged to use localized, less technically and meaningful messages.
     * Note that response status text is valid only after request is successfully
     * complete.
     *
     * @return String response status English description.
     */
    getStatusText: function() {
		$assert(this._state === js.net.XHR.StateMachine.DONE, 'js.net.XHR#getStatusText', 'Illegal state.');
        return this._request.statusText;
    },

	/**
	 * Open connection with server. Open connection with server and initialize default
	 * request header values. These values can be overridden by calling {@link #setHeader}
	 * or {@link #setRequestType}.
	 *
	 * @param js.net.Method method HTTP method,
	 * @param String url remote resource URL,
	 * @param Boolean... async optional asynchronous operation mode, default to true,
	 * @param String... user optional user name for authentication,
	 * @param String... password optional password, mandatory if user present.
     * @return js.net.XHR this object.
	 * @assert all arguments are not undefined, null or empty and of proper type, if present.
	 */
	open: function(method, url, async, user, password) {
		$assert(this._state === js.net.XHR.StateMachine.CREATED, 'js.net.XHR#open', 'Illegal state.');
		this._state = js.net.XHR.StateMachine.OPENED;

	    $assert(method, 'js.net.XHR#open', 'Undefined or null method.');
	    $assert(url, 'js.net.XHR#open', 'Undefined or null URL.');
		$assert(typeof async === 'undefined' || js.lang.Types.isBoolean(async), 'js.net.XHR#open', 'Asynchronous flag is not boolean.');
		$assert(typeof user === 'undefined' || js.lang.Types.isString(user), 'js.net.XHR#open', 'User is not string.');
		$assert(typeof password === 'undefined' || js.lang.Types.isString(password), 'js.net.XHR#open', 'Password is not string.');

		if(typeof async === 'undefined') {
			async = true;
		}
	    /**
	     * Synchronous mode flag. All XHR transaction are asynchronous, i.e. after send
	     * returns immediately and invoker should use events to acquire server response.
	     * So this flag is false by default. Anyway, there are marginal use cases where
	     * a synchronous response is more appropriate.
	     * @type Boolean
	     * @private
	     */
	    this._synchronousMode = !async;
		if(this._synchronousMode && this._timeout.get() === 0) {
			this._timeout.set(js.net.XHR.SYNC_TIMEOUT);
		}
	    if (async) {
	        this._request.onreadystatechange = this._onReadyStateChange.bind(this);
	    }
	    this._request.open(method, url, async, user, password);

	    this._request.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
	    this._request.setRequestHeader(js.net.XHR.VERSION_HEADER, JSLIB_VERSION);
	    this._request.setRequestHeader(js.net.XHR.REQUEST_TYPE_HEADER, 'XHR');
	    this._request.setRequestHeader('Cache-Control', 'no-cache');
	    this._request.setRequestHeader('Cache-Control', 'no-store');
	    this._request.setRequestHeader('Accept', 'application/json, text/xml, text/plain');
		return this;
	},

	/**
	 * Send request to server.
	 *
	 * @param Object data
	 * @return Object server response if this transaction is synchronously.
	 */
    send: function(data) {
		$assert(this._state === js.net.XHR.StateMachine.OPENED, 'js.net.XHR#send', 'Illegal state.');
		this._state = js.net.XHR.StateMachine.SENDING;

		// send void --------------------------------------
		if(typeof data === 'undefined' || data === null) {
	        this._request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
	        this._timeout.start();
	        this._request.send();
		}

		// send string ------------------------------------
        else if (js.lang.Types.isString(data)) {
	        this._request.setRequestHeader('Content-Type', 'text/plain; charset=UTF-8');
	        this._timeout.start();
	        this._request.send(data);
	  	}

		// send document ----------------------------------
        else if (data instanceof js.dom.Document) {
	        this._request.setRequestHeader('Content-Type', 'text/xml; charset=UTF-8');
	        this._timeout.start();
	        this._request.send(data._document);
        }

		// send form --------------------------------------
        else if (data instanceof js.dom.Form) {
	        // relies on browser to set the proper multipart content type and boundaries
	        this._request.send(new FormData(data.getNode()));
	        // upload duration may naturally vary from seconds to minutes and is hardly predictable
	        // for this reason sending forms doesn't use timeout but relies on abort and progress events
        }

		// send object ------------------------------------
        else {
	        this._request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
	        this._timeout.start();
	        this._request.send(js.lang.JSON.stringify(data));
        }

        if (this._synchronousMode) {
			this._timeout.stop();
            var res = this._processResponse();
            this.finalize();
            return res;
        }
    },

    /**
     * Abort.
     */
    abort: function() {
		// here we face a race condition
		// send has executed; as a consequence a separate thread is created for XHR transaction
		// we are in current thread executing abort but there is no guaranty meanwhile transaction
		// thread was not already executed finalization from ready state handler

		try {
			this._request.onreadystatechange = null;
			this._timeout.stop();
			this._request.abort();
			this._state = js.net.XHR.StateMachine.ABORTED;
			this._events.fire('loadend');
			this.finalize();
		}
		catch(er) {
			this._log.error('js.net.XHR$abort', er);
		}
    },

    /**
     * Ready state handler. Actually waits for {@link js.net.ReadyState DONE} and
     * takes next actions:
     * <ul>
     * <li>stop timer, if this instance has one
     * <li>if not on abort executes next 2 steps
     * <li>invoke {@link #_processResponse}, responsible for server response processing
     * <li>if server side is not in error fires <b>load</b> event
     * <li>in any case fires <b>loadend</b> event and cleanup this instance
     * </ul>
     * Any raised exception, other than server side error is signaled via global
     * error handler, which usually just alert.
     */
    _onReadyStateChange: function() {
        if (this._request.readyState === js.net.ReadyState.DONE) {
            try {
                this._timeout.stop();
            	var response = this._processResponse();
				if (typeof response !== 'undefined') {
			        this._events.fire('load', response);
				}
            }
            catch (er) {
                js.ua.Window.error(er);
            }
            finally {
				try {
					this._events.fire('loadend');
				}
				catch(er){
					this._log.error('js.net.XHR$_onReadyStateChange', 'Error on loadend listeners: %s', er);
				}
				this.finalize();
            }
        }
    },

    /**
     * Process server response. Is invoked by {@link #_onReadyStateChange ready state handler};
     * if request is synchronously ready state handler is not used and this method
     * is invoked directly by {@link #send}. In any case server response is fully
     * loaded, both headers and data.
     *
     * <p>First check server status; on server error fires <b>error</b> event and
     * returns undefined. If there is no error event listener delegates
     * {@link js.ua.Window#error global error handler}.
     *
     * <p>Then choose the proper logic to parse server data, based on received content
     * type and return parser product. If content type is text/xml returned object
     * is an instance of {@link js.dom.Document}; if application/json returns an
     * application {@link Object}. Otherwise returns a {@link String}.
     *
     * @return Object string, object or XML document sent back by server.
     */
    _processResponse: function() {
        var version = this._request.getResponseHeader(js.net.XHR.VERSION_HEADER);
        if (version !== JSLIB_VERSION) {
            this._log.error('js.net.XHR$_processResponse', 'Server code base version [%s] mismatch client [%s].', version, JSLIB_VERSION);
        }

        var er = this._getError(this._request);
        if (typeof er !== 'undefined') {
			this._log.debug('js.net.XHR$_processResponse', 'Server side error: %s', er);
	        if (this._events.hasListener('error')) {
            	this._events.fire('error', er);
        	}
        	else {
            	js.ua.Window.error(er);
        	}
			this._state = js.net.XHR.StateMachine.ERROR;
            return undefined;
        }

        // process server response considering its content type
		this._state = js.net.XHR.StateMachine.DONE;
        var contentType = this._request.getResponseHeader('Content-Type');
        if (contentType && contentType.indexOf('xml') !== -1) {
            return new js.dom.Document(this._request.responseXML);
        }
        if (contentType && contentType.indexOf('json') !== -1) {
            return js.lang.JSON.parse(this._request.responseText);
        }
        // content type is neither JSON or XML; process it as text
        return this._request.responseText;
    },

    /**
     * Timeout handler.
     */
    _onTimeout: function() {
        this._events.fire('timeout');
        this.abort();
    },

    /**
     * Get error. Return this request status error message or undefined if status is OK.
     *
     * @param Object request
     * @return String request status error or undefined.
     */
    _getError: function(request) {
        var s = request.status;
        if (s < 200 || s >= 300) {
            return $format('Bad HTTP response status: [%s] %s', s, request.statusText);
        }
    },

    /**
     * Returns a string representation of the object.
     *
     * @return String object string representation.
     */
    toString: function() {
        return 'js.net.XHR';
    },

    /**
     * Finalize this instance. Dispose used resources so that this object become invalid.
     * The behavior of this class is not specified if attempt to use any of its methods
     * after finalization. Note that this class implements self-destruction; finalize is
     * internally invoked after firing loadend event.
     */
    finalize: function() {
        this._events.finalize();
        delete this._events;
        delete this._request;
        delete this._timeout;
    }
};
$extends(js.net.XHR, js.lang.Object);

/**
 * XHR state machine.
 *
 * @author Iulian Rotaru
 * @since 1.0
 */
js.net.XHR.StateMachine = {
	/**
	 * XHR instance just created.
	 */
	CREATED: 0,

	/**
	 * Open method was invoked.
	 */
	OPENED: 1,

	/**
	 * Sending pending.
	 */
	SENDING: 2,

	/**
	 * User abort.
	 */
	ABORTED: 3,

	/**
	 * Completed with success.
	 */
	DONE: 4,

	/**
	 * Completed with server error.
	 */
	ERROR: 5
};

$legacy(js.ua.Engine.TRIDENT, function() {
	/**
	 * @param XMLHttpRequest request
	 * @return String
	 */
    js.net.XHR.prototype._getError = function(request) {
        var s = request.status;
        // 1223 == 204
        if (s && (s < 200 || s >= 300) && s != 1223) {
            return $format('Bad HTTP response status: [%s] %s', s, request.statusText);
        }
    };
});

$legacy(typeof FormData === 'undefined', function() {
	$suppress('NO_FIELD_DECL');

	/**
	 * @param String type
	 * @param Function listener
	 * @param Object scope
	 * @return js.net.XHR
	 */
	js.net.XHR.prototype.on = function(type, listener, scope) {
		$assert(this._state === js.net.XHR.StateMachine.CREATED, 'js.net.XHR#on', 'Illegal state.');
        if (type === 'progress') {
			this._hasProgress = true;
        }
        this._events.addListener(type, listener, scope || window);
        return this;
	};

	js.net.XHR.prototype._onProgress = function(progress) {
        this._events.fire('progress', progress);
	};

	js.net.XHR.prototype._open = js.net.XHR.prototype.open;

	/**
	 * @param js.net.Method method
	 * @param String url
	 * @param Boolean async
	 * @param String user
	 * @param String password
	 * @return js.net.XHR
	 */
	js.net.XHR.prototype.open = function(method, url, async, user, password) {
		$assert(this._state === js.net.XHR.StateMachine.CREATED, 'js.net.XHR#open', 'Illegal state.');
		this._state = js.net.XHR.StateMachine.OPENED;

	    $assert(method, 'js.net.XHR#open', 'Undefined or null method.');
	    $assert(url, 'js.net.XHR#open', 'Undefined or null URL.');
		$assert(typeof async === 'undefined' || js.lang.Types.isBoolean(async), 'js.net.XHR#open', 'Asynchronous flag is not boolean.');
		$assert(typeof user === 'undefined' || js.lang.Types.isString(user), 'js.net.XHR#open', 'User is not string.');
		$assert(typeof password === 'undefined' || js.lang.Types.isString(password), 'js.net.XHR#open', 'Password is not string.');

		this._method = method;
		this._url = url;
		this._async = async;
		this._user = user;
		this._password = password;
	    this._headers = [];
		return this;
	};

	/**
	 * @param String header
	 * @param String value
	 * @return js.net.XHR
	 */
	js.net.XHR.prototype._setHeader = function(header, value) {
		this._headers.push({
			header: header,
			value: value
		});
		return this;
	};

	js.net.XHR.prototype._send = js.net.XHR.prototype.send;

	js.net.XHR.prototype.send = function(data) {
		var res;
		if (data instanceof js.dom.Form) {
			this._request.abort();
			delete this._requets;
			this._request = new js.net.XHR.Upload(this);

			var form = data;
			form.setAction(this._url);
			this._request.send(form);
		}
		else {
			this._state = js.net.XHR.StateMachine.CREATED;
			this._open(this._method, this._url, this._async, this._user, this._password);
			for(var i = 0, item; i < this._headers.length; ++i) {
				item = this._headers[i];
	            this._request.setRequestHeader(item.header, item.value);
	        }
			res = this._send(data);
		}
		return res;
	};

	js.net.XHR.prototype._finalize = js.net.XHR.prototype.finalize;

	js.net.XHR.prototype.finalize = function() {
		delete this._method;
		delete this._url;
		delete this._async;
		delete this._user;
		delete this._password;
		delete this._headers;
		if (this._request instanceof js.net.XHR.Upload) {
			this._request.finalize();
		}
		this._finalize();
	} ;

    /**
     * Asynchronous upload handler. This private class is used by {@link js.net.XHR}
     * class when data to be sent is a {@link js.dom.Form} instance.
     *
     * Important notes:
     * <ol>
     * <li>Because <i>asynchronous</i> feat is implemented with hidden iframe used
     * as form target, server response content type must be <b>text/html</b>; specifically
     * do not use <b>application/json</b>. Otherwise browser will try to open server
     * response instead to pass it to the hidden iframe - as a consequence iframe
     * on load event is not triggered.
     * <li>Browser security doesn't allow accessing a child frame document if it's
     * loaded from a different domain. For this reason do not try to set upload
     * URI to different domain, our recommendation being to use relative path.
     * </ol>
     *
     * @private
     * @author Iulian Rotaru
     * @since 1.0
     * @constructor
     *
     * @param js.net.XHR transaction parent XHR transaction.
     */
    js.net.XHR.Upload = function(transaction) {
        $assert(transaction instanceof js.net.XHR, 'js.net.XHR.Upload#Upload', 'Transaction is not instance of js.net.XHR');

		/** @type js.net.XHR */
	   	this._transaction = transaction;
        if (transaction._hasProgress) {
			/** @type js.util.Timer */
            this._progressTimer = new js.util.Timer(js.net.XHR.Upload.PROGRESS_INTERVAL);
            this._progressTimer.setCallback(this._onProgressTimer, this);
        }

		/** @type js.net.ReadyState */
        this.readyState = js.net.ReadyState.UNSET;
		/** @type Date */
		this._timestamp = null;
		/** @type Boolean */
		this._send = false;
		/** @type Number */
        this.status = 200;
		/** @type String */
        this.statusText = 'OK';
		/** @type String */
        this.responseText = '{}';

		/** @type js.net.XHR.ControlRequest */
		this._controlRequest = null;
		/** @type Boolean */
		this._controlRequestPending = false;
		/** @type js.dom.IFrame */
		this._iframe = null;
		/** @type js.util.UUID */
		this._uploadUUID = null;
		/** @type Object */
		this._responseHeaders = null;
    };

    /**
     * Form upload progress notifications timeout. This is the time allowed for status response to come back.
     */
    js.net.XHR.Upload.PROGRESS_TIMEOUT = 4000;

    /**
     * Form upload progress notifications interval.
     */
    js.net.XHR.Upload.PROGRESS_INTERVAL = 200;

    js.net.XHR.Upload.prototype =
    {
		/**
		 * @param js.dom.Form form
		 * @return js.net.XHR.Upload
		 */
        send: function(form) {
            this.readyState = js.net.ReadyState.OPENED;

            // control request is sent to the same URL as the form action
            this._controlRequest = new js.net.XHR.ControlRequest(form.getAction());
            this._controlRequest.setCallback(this._onControlResponse, this);

			var doc = form.getDocument();
            var id = js.util.ID();
            this._iframe = doc.createElement('iframe', 'id', id, 'name', id, 'src', 'about:blank');
            this._iframe.style.set(
            {
                'position': 'absolute',
                'top': '-1000px',
                'left': '-1000px'
            });
           	this._iframe.on('load', this._onIFrameLoaded, this);
            doc.getByTag('body').addChild(this._iframe);
            form.setAttr('target', this._iframe.getAttr('id'));

            this._uploadUUID = js.util.UUID();
            form.add('upload-uuid', this._uploadUUID);
            // access DOM native form node in order to avoid submit validation performed by js form classe(s)
            // also form send uses js.net.XHR.send which on its turn call this method... circular invocations
            form.getNode().submit();

            if (this._progressTimer) {
                this._progressTimer.start();
            }
            return this;
        },

		/**
		 * Inner frame loaded.
		 */
        _onIFrameLoaded: function() {
			$assert(this._iframe.getLocation() !== 'about:blank', 'js.net.XHR.Upload$_onIFrameLoaded', 'Load event generated by blank iframe.');

            if (this._progressTimer) {
                this._progressTimer.stop();
            }
			var doc = this._iframe.getDoc();
			this._responseHeaders = {};
			var it = doc.findByTag('meta').it(), meta;
			while(it.hasNext()) {
				meta = it.next();
				this._responseHeaders[meta.getAttr('http-equiv')] = meta.getAttr('content');
			}

            this.responseText = doc.getByTag('body').getText();
            this.readyState = js.net.ReadyState.DONE;
            this._transaction._onReadyStateChange();
		},

		/**
		 * Abort.
		 */
        abort: function() {
            this.status = 0;
            this.statusText = 'USER ABORT';
            if (this._progressTimer) {
                this._progressTimer.stop();
            }
            this._iframe.reload(); // reload iframe to signal engine to stop upload
            this._iframe.remove();
            delete this._iframe;
            this._sendControlRequest('ABORT');
        },

		/**
		 * @param String header
		 * @return String
		 */
        getResponseHeader: function(header) {
			var h = this._responseHeaders[header];
            return h? h: null;
        },

		/**
		 * Progress timer.
		 */
        _onProgressTimer: function() {
            var now = new Date().getTime();
            if (now - this._timestamp > js.net.XHR.Upload.PROGRESS_TIMEOUT) {
                this._controlRequest.abort();
                this._controlRequestPending = false;
            }
            if (!this._controlRequestPending) {
                this._sendControlRequest('STATUS');
            }
        },

        /**
         * Send upload control request. Post it to the same URL as uploading form, that is
         * form action attribute. Server code can use Content-Type to keep apart upload
         * control request, looking after 'application/json'.
         *
         * @param String opcode operation code. For now only STATUS and ABORT.
         */
        _sendControlRequest: function(opcode) {
            this._controlRequestPending = true;
            this._timestamp = new Date().getTime();
            this._controlRequest.send(
            {
                uploadUUID: this._uploadUUID,
                opcode: opcode
            });
        },

		/**
		 * Control response.
		 *
		 * @param String res
		 */
        _onControlResponse: function(res) {
            this['_on' + res.opcode](res.value);
            this._controlRequestPending = false;
        },

		/**
		 * Abort response.
		 */
        _onABORT: function() {
            this._send = false;
        },

		/**
		 * Status response.
		 *
		 * @param Object progress
		 */
        _onSTATUS: function(progress) {
            if (this._transaction) { // test against race condition
                this._transaction._onProgress(progress);
            }
        },

		/**
		 * Finalize.
		 */
        finalize: function() {
            this._iframe.remove();
            delete this._iframe;
            if (this._progressTimer) {
				this._progressTimer.stop();
                delete this._progressTimer;
            }
            this._controlRequest.finalize();
            delete this._controlRequest;
            delete this._transaction;
        }
    };

    /**
     * Control request.
     *
     * @private
     * @author Iulian Rotaru
     * @since 1.0
     * @constructor
     *
     * @param String url
     */
    js.net.XHR.ControlRequest = function(url) {
		/** @type String */
        this._url = url;
		/** @type Boolean */
        this._send = false;
		/** @type XMLHttpRequest */
        this._xhr = new XMLHttpRequest();
		/** @type Function */
		this._callback = null;
		/** @type Object */
		this._scope = null;
    };

    js.net.XHR.ControlRequest.prototype =
    {
		/** @type String */
        _contentType: 'application/json; charset=UTF-8',

		/**
		 * Set callback.
		 *
		 * @param Function callback
		 * @param Object scope
		 */
        setCallback: function(callback, scope) {
            this._callback = callback;
            this._scope = scope || window;
        },

		/**
		 * Send
		 *
		 * @param Object req
		 */
        send: function(req) {
            if (this._send) {
                this._xhr.abort();
            }
            this._send = true;
            this._xhr.onreadystatechange = this._onReadyStateChange.bind(this);

            this._xhr.open('POST', this._url, true);
            this._xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
            this._xhr.setRequestHeader(js.net.XHR.VERSION_HEADER, JSLIB_VERSION);
            this._xhr.setRequestHeader(js.net.XHR.REQUEST_TYPE_HEADER, 'UPLOAD');
            this._xhr.setRequestHeader('Cache-Control', 'no-cache');
            this._xhr.setRequestHeader('Cache-Control', 'no-store');
            this._xhr.setRequestHeader('Accept', this._contentType);
            this._xhr.setRequestHeader('Content-Type', this._contentType);
            this._xhr.send(js.lang.JSON.stringify(req));
        },

		/**
		 * Ready state change.
		 */
		_onReadyStateChange: function() {
            if (this._xhr.readyState === js.net.ReadyState.DONE) {
                this._callback.call(this._scope, js.lang.JSON.parse(this._xhr.responseText));
                this._send = false;
            }
		},

		/**
		 * Abort.
		 */
        abort: function() {
            this._xhr.abort();
            this._send = false;
        },

		/**
		 * Finalize.
		 */
        finalize: function() {
            delete this._xhr;
        }
    };
});
