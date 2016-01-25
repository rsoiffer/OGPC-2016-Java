uniform sampler2D texture1;

uniform int size;

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec2 texelSize = 1.0 / textureSize(texture1, 0);
    vec2 texelSize05 = texelSize * 0.5;

    vec2 uvOffset = texelSize.xy * vec2(size) + texelSize05;

    vec2 texCoordSample;
    vec4 color;

    texCoordSample.x = uv.x - uvOffset.x;
    texCoordSample.y = uv.y + uvOffset.y;
    color = texture2D( texture1, texCoordSample );

    texCoordSample.x = uv.x + uvOffset.x;
    texCoordSample.y = uv.y + uvOffset.y;
    color += texture2D( texture1, texCoordSample );

    texCoordSample.x = uv.x + uvOffset.x;
    texCoordSample.y = uv.y - uvOffset.y;
    color += texture2D( texture1, texCoordSample );

    texCoordSample.x = uv.x - uvOffset.x;
    texCoordSample.y = uv.y - uvOffset.y;
    color += texture2D( texture1, texCoordSample );

    gl_FragColor = color * 0.25;
}
