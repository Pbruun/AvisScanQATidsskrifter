# OpenAPI generator templates

Copied and adapted from https://github.com/OpenAPITools/openapi-generator/
to provide streaming support.


# API and Impl, concrete source unknown, adapted for streaming support

 * `api.mustache`
 * `apiServiceImpl.mustache`
 * `returnTypes.mustache`

pojo.mustache taken from

<https://github.com/OpenAPITools/openapi-generator/blob/v4.2.2/modules/openapi-generator/src/main/resources/JavaJaxRS/cxf-ext/pojo.mustache>

Adapted 
`@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss.SSSX")`
to
`@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")`
to use 24-hour timestamps...
