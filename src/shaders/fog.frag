uniform sampler2D texture1;
uniform vec4 min;
uniform float fade;
uniform float density;
uniform vec3 fogColor;

void main() {
    float dist = gl_FragCoord.z / gl_FragCoord.w;
    float fogAmount = pow(2.0, -dist * dist * density);

    vec4 texColor = max(min, texture2D(texture1, gl_TexCoord[0].st));
    gl_FragColor = mix(vec4(fogColor, 1), gl_Color * texColor, fogAmount);
/*
    if (texColor.a > .5) {
        gl_FragColor = vec4(mix(fogColor, gl_Color.rgb * texColor.rgb * vec3(fade), fogAmount) , 1);
    } else {
        gl_FragColor = vec4(0);
    }
*/
}