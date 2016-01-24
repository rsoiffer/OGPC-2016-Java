uniform sampler2D texture1;

uniform bool horizontal;

uniform float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

void main() {
    vec2 tex_offset = 1.0 / textureSize(texture1, 0); // gets size of single texel
    vec4 result = texture2D(texture1, gl_TexCoord[0]) * weight[0]; // current fragment's contribution
    if(horizontal) {
        for(int i = 1; i < 5; ++i) {
            result += texture2D(texture1, gl_TexCoord[0].st + vec2(tex_offset.x * i, 0)) * weight[i];
            result += texture2D(texture1, gl_TexCoord[0].st - vec2(tex_offset.x * i, 0)) * weight[i];
        }
    } else {
        for(int i = 1; i < 5; ++i) {
            result += texture2D(texture1, gl_TexCoord[0].st + vec2(0, tex_offset.y * i)) * weight[i];
            result += texture2D(texture1, gl_TexCoord[0].st - vec2(0, tex_offset.y * i)) * weight[i];
        }
    }
    gl_FragColor = vec4(result.rgb, 1);
}