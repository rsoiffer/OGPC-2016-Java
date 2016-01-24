uniform sampler2D texture1;

void main() {
    gl_FragColor = vec4(max(vec3(0), (texture2D(texture1, gl_TexCoord[0].st) * gl_Color).rgb - vec3(1)), 1);
}