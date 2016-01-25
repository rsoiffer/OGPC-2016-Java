uniform sampler2D texture1;

void main() {
    vec4 c = texture2D(texture1, gl_TexCoord[0].st);
    gl_FragColor = vec4(c.rgb * .9, c.a);
}