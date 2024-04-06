# Multiple additional fields , result can not have multiple field
public type User05 record {|
    *Pet;
    string? name?;
    int? id?;
    int?|string[]?...;
|};

# Additional properties with type object without properties
public type User06 record {|
    *Pet;
    string? name?;
    int? id?;
    record {}?...;
|};

# Additional properties with object with property fields
public type User07 record {|
    *Pet;
    string? name?;
    int? id?;
    record {string? country?; string? state?;}?...;
|};

# Reference has additional properties.
public type User08 record {|
    *Pet02;
    string? name?;
    int? id?;
    Pet?|int...;
|};

# Reference has additional properties with nullable true.
public type User09 record {|
    *Pet02;
    string? name?;
    int? id?;
    Pet?|int?...;
|};

# Mock record 02
public type Pet02 record {|
    string? name?;
    int? age?;
    Pet?...;
|};

public type Store_inventory_body record {
    Pet? pet?;
    Pet02? pet2?;
    User01? user1?;
    User02? user2?;
    User03? user3?;
    User04? user4?;
    User05? user5?;
    User06? use6?;
    User07? user7?;
    User08? user8?;
    User09? use9?;
};

# Without any additional field it maps to closed record.
public type User01 record {
    *Pet;
    string? name?;
    int? id?;
};

# Additional properties with `true` enable
public type User02 record {
    *Pet;
    string? name?;
    int? id?;
};

# Mock record
public type Pet record {
    string? name?;
    int? age?;
};

# Additional properties with {}
public type User03 record {
    *Pet;
    string? name?;
    int? id?;
};

# Additional properties with type string
public type User04 record {|
    *Pet;
    string? name?;
    int? id?;
    string?...;
|};
