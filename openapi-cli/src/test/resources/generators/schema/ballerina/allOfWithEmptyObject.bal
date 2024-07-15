public type Pets Pet[];

public type ErrorDefault record {|
    *http:DefaultStatusCodeResponse;
    Error body;
|};

public type Error record {
    int code;
    string message;
};

public type Pet record {
    string[] entries?;
};
