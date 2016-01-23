uniform sampler2D texture1;

void main() {
    gl_FragColor = pow(texture2D(texture1, gl_TexCoord[0].st), vec4(vec3(.45), 1));
}