uniform sampler2D texture1;
uniform vec2 wobble;

void main() {
    gl_FragColor = texture2D(texture1, gl_TexCoord[0].st + wobble);
}